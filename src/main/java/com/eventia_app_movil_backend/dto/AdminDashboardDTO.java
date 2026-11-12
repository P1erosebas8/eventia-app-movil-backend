package com.eventia_app_movil_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    private Double totalRevenue;
    private Integer totalTicketsSold;
    private Map<String, Integer> salesByCategory;
    private Map<String, Double> revenueByCategory;
    private List<PopularEventDTO> popularEvents;
    private List<EventStockMetricDTO> stockMetrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularEventDTO {
        private String id;
        private String title;
        private Integer ticketsSold;
        private Double revenue;
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventStockMetricDTO {
        private String id;
        private String title;
        private String category;
        private Integer totalInitialStock;
        private Integer currentAvailableStock;
        private Integer ticketsSold;
        private Integer percentSold;
    }
}

