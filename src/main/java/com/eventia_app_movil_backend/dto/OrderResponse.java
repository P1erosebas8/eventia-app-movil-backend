package com.eventia_app_movil_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private Double totalAmount;
    private Double discountAmount;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private List<TicketResponseDTO> tickets;
}

