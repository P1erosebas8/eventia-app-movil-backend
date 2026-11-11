package com.eventia_app_movil_backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private Long userId;
    private String buyerName;
    private String buyerEmail;
    private String buyerDocument;
    private String paymentMethod;
    private Double totalAmount;
    private Double discountAmount;

    @NotEmpty(message = "Debe incluir al menos un ítem en la orden")
    private List<OrderItemRequest> items;
}

