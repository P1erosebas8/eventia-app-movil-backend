package com.eventia_app_movil_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Double amount;
    private String currency;
    private String email;
    private String cardNumber;
    private String cvv;
    private String expirationMonth;
    private String expirationYear;
    private String cardHolder;
    private Integer installments;
    private Long orderId;
}

