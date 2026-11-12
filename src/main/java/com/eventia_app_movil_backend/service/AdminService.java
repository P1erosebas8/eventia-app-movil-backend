package com.eventia_app_movil_backend.service;

import com.eventia_app_movil_backend.dto.AdminDashboardDTO;
import com.eventia_app_movil_backend.model.Event;
import com.eventia_app_movil_backend.model.Order;
import com.eventia_app_movil_backend.model.OrderDetail;
import com.eventia_app_movil_backend.model.PaymentStatus;
import com.eventia_app_movil_backend.model.TicketType;
import com.eventia_app_movil_backend.repository.EventRepository;
import com.eventia_app_movil_backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardMetrics() {
        List<Order> orders = orderRepository.findAll();
        List<Event> events = eventRepository.findAll();

        double totalRevenue = 0.0;
        int totalTicketsSold = 0;

        int salesGeneral = 0;
        int salesVip = 0;
        int salesVipPlus = 0;

        double revGeneral = 0.0;
        double revVip = 0.0;
        double revVipPlus = 0.0;

        // Mapa de eventId -> {ticketsSold, revenue}
        Map<Long, Integer> eventTicketsMap = new HashMap<>();
        Map<Long, Double> eventRevenueMap = new HashMap<>();

        for (Order order : orders) {
            // Contabilizar si está pagada o sin estado explícito
            if (order.getPaymentStatus() == PaymentStatus.PAID || order.getPaymentStatus() == null) {
                totalRevenue += (order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);

                if (order.getOrderDetails() != null) {
                    for (OrderDetail detail : order.getOrderDetails()) {
                        int qty = detail.getQuantity() != null ? detail.getQuantity() : 0;
                        double price = detail.getUnitPrice() != null ? detail.getUnitPrice() : 0.0;
                        double itemRev = (detail.getSubtotal() != null) ? detail.getSubtotal() : (price * qty);

                        totalTicketsSold += qty;

                        String ticketName = (detail.getTicketType() != null && detail.getTicketType().getName() != null)
                                ? detail.getTicketType().getName().toLowerCase()
                                : "";

                        if (ticketName.contains("+") || ticketName.contains("plus")) {
                            salesVipPlus += qty;
                            revVipPlus += itemRev;
                        } else if (ticketName.contains("vip")) {
                            salesVip += qty;
                            revVip += itemRev;
                        } else {
                            salesGeneral += qty;
                            revGeneral += itemRev;
                        }

                        if (detail.getTicketType() != null && detail.getTicketType().getEvent() != null) {
                            Long eventId = detail.getTicketType().getEvent().getId();
                            eventTicketsMap.put(eventId, eventTicketsMap.getOrDefault(eventId, 0) + qty);
                            eventRevenueMap.put(eventId, eventRevenueMap.getOrDefault(eventId, 0.0) + itemRev);
                        }
                    }
                }
            }
        }

        Map<String, Integer> salesByCategory = new HashMap<>();
        salesByCategory.put("general", salesGeneral);
        salesByCategory.put("vip", salesVip);
        salesByCategory.put("vipPlus", salesVipPlus);

        Map<String, Double> revenueByCategory = new HashMap<>();
        revenueByCategory.put("general", Math.round(revGeneral * 100.0) / 100.0);
        revenueByCategory.put("vip", Math.round(revVip * 100.0) / 100.0);
        revenueByCategory.put("vipPlus", Math.round(revVipPlus * 100.0) / 100.0);

        List<AdminDashboardDTO.PopularEventDTO> popularEvents = events.stream()
                .filter(Event::isActive)
                .map(e -> {
                    int sold = eventTicketsMap.getOrDefault(e.getId(), 0);
                    double rev = eventRevenueMap.getOrDefault(e.getId(), 0.0);
                    return AdminDashboardDTO.PopularEventDTO.builder()
                            .id(String.valueOf(e.getId()))
                            .title(e.getTitle())
                            .ticketsSold(sold)
                            .revenue(Math.round(rev * 100.0) / 100.0)
                            .imageUrl(e.getBannerUrl())
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()))
                .limit(5)
                .collect(Collectors.toList());

        List<AdminDashboardDTO.EventStockMetricDTO> stockMetrics = events.stream()
                .filter(Event::isActive)
                .map(e -> {
                    int sold = eventTicketsMap.getOrDefault(e.getId(), 0);
                    int currentAvailable = 0;
                    if (e.getTicketTypes() != null) {
                        for (TicketType tt : e.getTicketTypes()) {
                            if (tt.isActive()) {
                                currentAvailable += (tt.getAvailableCapacity() != null ? tt.getAvailableCapacity() : 0);
                            }
                        }
                    }
                    int totalInitial = currentAvailable + sold;
                    int percent = totalInitial > 0 ? (int) Math.round(((double) sold / totalInitial) * 100.0) : 0;
                    String categoryName = (e.getCategory() != null) ? e.getCategory().getName() : "General";

                    return AdminDashboardDTO.EventStockMetricDTO.builder()
                            .id(String.valueOf(e.getId()))
                            .title(e.getTitle())
                            .category(categoryName)
                            .totalInitialStock(totalInitial)
                            .currentAvailableStock(currentAvailable)
                            .ticketsSold(sold)
                            .percentSold(percent)
                            .build();
                })
                .collect(Collectors.toList());

        return AdminDashboardDTO.builder()
                .totalRevenue(Math.round(totalRevenue * 100.0) / 100.0)
                .totalTicketsSold(totalTicketsSold)
                .salesByCategory(salesByCategory)
                .revenueByCategory(revenueByCategory)
                .popularEvents(popularEvents)
                .stockMetrics(stockMetrics)
                .build();
    }
}
