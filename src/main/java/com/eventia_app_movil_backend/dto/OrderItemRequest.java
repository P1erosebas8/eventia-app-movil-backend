package com.eventia_app_movil_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    @NotNull(message = "El ID del tipo de entrada es requerido")
    private Long ticketTypeId;

    @NotNull(message = "La cantidad es requerida")
    private Integer quantity;

    private Double unitPrice;
}

