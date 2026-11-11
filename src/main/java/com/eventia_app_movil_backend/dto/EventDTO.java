package com.eventia_app_movil_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String venue;
    private Integer capacity;
    private String bannerUrl;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private String category;
    private String organizer;
    private String time;
    private String status;
    private Boolean active;
    private List<TicketTypeDTO> ticketTypes;
    private List<TicketTypeDTO> tickets;
}
