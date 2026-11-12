package com.eventia_app_movil_backend.service;

import com.eventia_app_movil_backend.dto.*;
import com.eventia_app_movil_backend.model.*;
import com.eventia_app_movil_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final TicketRepository ticketRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, String authenticatedEmail) {
        User user = null;

        if (authenticatedEmail != null && !authenticatedEmail.isBlank()) {
            user = userRepository.findByEmail(authenticatedEmail).orElse(null);
        }

        if (user == null && request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }

        if (user == null && request.getBuyerEmail() != null) {
            user = userRepository.findByEmail(request.getBuyerEmail()).orElse(null);
        }

        // Si aún no existe, creamos un usuario invitado/cliente al vuelo
        if (user == null) {
            String email = (request.getBuyerEmail() != null && !request.getBuyerEmail().isBlank())
                    ? request.getBuyerEmail()
                    : "invitado" + System.currentTimeMillis() + "@eventia.com";

            String name = (request.getBuyerName() != null && !request.getBuyerName().isBlank())
                    ? request.getBuyerName()
                    : "Cliente";

            String doc = (request.getBuyerDocument() != null && !request.getBuyerDocument().isBlank())
                    ? request.getBuyerDocument()
                    : "DNI" + System.currentTimeMillis() % 10000000;

            user = userRepository.save(User.builder()
                    .firstName(name)
                    .lastName("Eventia")
                    .email(email)
                    .dni(doc)
                    .password("$2a$10$dummyencryptedpassworderplaceholder")
                    .role(UserRole.CLIENT)
                    .active(true)
                    .build());
        }

        double calculatedTotal = 0.0;
        List<OrderDetail> detailsToSave = new ArrayList<>();

        Order order = Order.builder()
                .user(user)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CARD")
                .paymentStatus(PaymentStatus.PAID)
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : 0.0)
                .totalAmount(0.0)
                .build();

        Order savedOrder = orderRepository.save(order);

        List<Ticket> ticketsCreated = new ArrayList<>();

        for (OrderItemRequest item : request.getItems()) {
            TicketType ticketType = ticketTypeRepository.findById(item.getTicketTypeId())
                    .orElseGet(() -> ticketTypeRepository.findAll().stream()
                            .filter(t -> t.getAvailableCapacity() >= item.getQuantity())
                            .findFirst()
                            .orElseGet(() -> ticketTypeRepository.findAll().stream().findFirst()
                                    .orElseThrow(() -> new IllegalArgumentException("Tipo de entrada no encontrado"))));

            // Validar capacidad
            if (ticketType.getAvailableCapacity() < item.getQuantity()) {
                throw new IllegalStateException("No hay suficiente capacidad disponible para la entrada: " + ticketType.getName());
            }

            // Descontar capacidad
            ticketType.setAvailableCapacity(ticketType.getAvailableCapacity() - item.getQuantity());
            ticketTypeRepository.save(ticketType);

            double price = item.getUnitPrice() != null ? item.getUnitPrice() : ticketType.getPrice();
            double subtotal = price * item.getQuantity();
            calculatedTotal += subtotal;

            OrderDetail detail = OrderDetail.builder()
                    .order(savedOrder)
                    .ticketType(ticketType)
                    .quantity(item.getQuantity())
                    .unitPrice(price)
                    .discountApplied(0.0)
                    .subtotal(subtotal)
                    .build();

            OrderDetail savedDetail = orderDetailRepository.save(detail);

            // Generar los tickets digitales individuales
            for (int i = 0; i < item.getQuantity(); i++) {
                Ticket ticket = Ticket.builder()
                        .orderDetail(savedDetail)
                        .status(TicketStatus.VALID)
                        .build();
                ticketsCreated.add(ticketRepository.save(ticket));
            }

            detailsToSave.add(savedDetail);
        }

        savedOrder.setTotalAmount(request.getTotalAmount() != null ? request.getTotalAmount() : calculatedTotal);
        savedOrder.setOrderDetails(detailsToSave);
        orderRepository.save(savedOrder);

        return mapToOrderResponse(savedOrder, ticketsCreated);
    }

    public List<OrderResponse> getUserOrders(String email) {
        List<Order> orders = orderRepository.findByUserEmailOrderByCreatedAtDesc(email);
        return orders.stream()
                .map(this::mapOrderToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(this::mapOrderToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + id));
        return mapOrderToResponse(order);
    }

    public List<TicketResponseDTO> getUserTickets(String email) {
        List<Ticket> tickets = ticketRepository.findByUserEmail(email);
        return tickets.stream()
                .map(this::mapTicketToDTO)
                .collect(Collectors.toList());
    }

    public List<TicketResponseDTO> getUserTicketsById(Long userId) {
        List<Ticket> tickets = ticketRepository.findByUserId(userId);
        return tickets.stream()
                .map(this::mapTicketToDTO)
                .collect(Collectors.toList());
    }

    private OrderResponse mapOrderToResponse(Order order) {
        List<Ticket> allTickets = new ArrayList<>();
        if (order.getOrderDetails() != null) {
            for (OrderDetail od : order.getOrderDetails()) {
                if (od.getTickets() != null) {
                    allTickets.addAll(od.getTickets());
                }
            }
        }
        return mapToOrderResponse(order, allTickets);
    }

    private OrderResponse mapToOrderResponse(Order order, List<Ticket> tickets) {
        List<TicketResponseDTO> ticketDTOs = tickets.stream()
                .map(this::mapTicketToDTO)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userEmail(order.getUser() != null ? order.getUser().getEmail() : "")
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "PAID")
                .createdAt(order.getCreatedAt())
                .tickets(ticketDTOs)
                .build();
    }

    private TicketResponseDTO mapTicketToDTO(Ticket ticket) {
        String eventTitle = "Evento Eventia";
        String eventLocation = "Lima, Perú";
        String eventDate = "2026-11-20";
        String ticketTypeName = "General";
        Double price = 0.0;

        if (ticket.getOrderDetail() != null && ticket.getOrderDetail().getTicketType() != null) {
            TicketType tt = ticket.getOrderDetail().getTicketType();
            ticketTypeName = tt.getName();
            price = tt.getPrice();
            if (tt.getEvent() != null) {
                eventTitle = tt.getEvent().getTitle();
                eventLocation = tt.getEvent().getLocation();
                eventDate = tt.getEvent().getDate() != null ? tt.getEvent().getDate().toString() : "";
            }
        }

        return TicketResponseDTO.builder()
                .id(ticket.getId())
                .qrCode(ticket.getQrCode())
                .eventTitle(eventTitle)
                .eventLocation(eventLocation)
                .eventDate(eventDate)
                .ticketTypeName(ticketTypeName)
                .price(price)
                .status(ticket.getStatus() != null ? ticket.getStatus().name() : "VALID")
                .createdAt(ticket.getCreatedAt())
                .build();
    }
}

