package com.eventia_app_movil_backend.controller;

import com.eventia_app_movil_backend.dto.PaymentRequest;
import com.eventia_app_movil_backend.dto.PaymentResponse;
import com.eventia_app_movil_backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({"/payments", "/api/payments"})
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/charge")
    public ResponseEntity<?> charge(@RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = paymentService.processCharge(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

