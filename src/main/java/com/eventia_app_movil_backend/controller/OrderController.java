package com.eventia_app_movil_backend.controller;

import com.eventia_app_movil_backend.dto.OrderRequest;
import com.eventia_app_movil_backend.dto.OrderResponse;
import com.eventia_app_movil_backend.dto.TicketResponseDTO;
import com.eventia_app_movil_backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping({"/orders", "/api/orders"})
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request, Principal principal) {
        String userEmail = principal != null ? principal.getName() : request.getBuyerEmail();
        try {
            OrderResponse response = orderService.createOrder(request, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping({"/orders/my-orders", "/api/orders/my-orders"})
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Principal principal,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String email
    ) {
        if (principal != null) {
            return ResponseEntity.ok(orderService.getUserOrders(principal.getName()));
        } else if (email != null && !email.isBlank()) {
            return ResponseEntity.ok(orderService.getUserOrders(email));
        } else if (userId != null) {
            return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping({"/orders/{id}", "/api/orders/{id}"})
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping({"/tickets/my-tickets", "/api/tickets/my-tickets"})
    public ResponseEntity<List<TicketResponseDTO>> getMyTickets(
            Principal principal,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String email
    ) {
        if (principal != null) {
            return ResponseEntity.ok(orderService.getUserTickets(principal.getName()));
        } else if (email != null && !email.isBlank()) {
            return ResponseEntity.ok(orderService.getUserTickets(email));
        } else if (userId != null) {
            return ResponseEntity.ok(orderService.getUserTicketsById(userId));
        }
        return ResponseEntity.ok(List.of());
    }
}
