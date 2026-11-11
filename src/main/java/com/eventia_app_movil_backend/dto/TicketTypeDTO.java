package com.eventia_app_movil_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypeDTO {
    private Long id;
    private String name;
    private Double price;
    private Integer totalCapacity;
    private Integer availableCapacity;
    private String zone;
    private Boolean isPresale;
    private Integer maxPerPurchase;
    private LocalDate saleStartDate;
    private LocalDate saleEndDate;
    private Boolean active;

    // Campos compatibles con la app móvil (Stitch / React Native)
    public Integer getAvailable() {
        return availableCapacity != null ? availableCapacity : totalCapacity;
    }

    public Integer getTotal() {
        return totalCapacity;
    }
}
