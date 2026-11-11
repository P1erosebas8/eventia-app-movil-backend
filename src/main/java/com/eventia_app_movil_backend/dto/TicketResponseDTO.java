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
public class TicketResponseDTO {
    private Long id;
    private String qrCode;
    private String eventTitle;
    private String eventLocation;
    private String eventDate;
    private String ticketTypeName;
    private Double price;
    private String status;
    private LocalDateTime createdAt;
}

