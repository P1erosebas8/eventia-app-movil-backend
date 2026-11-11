package com.eventia_app_movil_backend.service;

import com.eventia_app_movil_backend.dto.PaymentRequest;
import com.eventia_app_movil_backend.dto.PaymentResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    public PaymentResponse processCharge(PaymentRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero.");
        }

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String authCode = "AUTH-" + (int) (Math.random() * 900000 + 100000);

        return PaymentResponse.builder()
                .success(true)
                .message("Pago procesado exitosamente")
                .transactionId(transactionId)
                .authorizationCode(authCode)
                .status("PAID")
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "PEN")
                .build();
    }
}

