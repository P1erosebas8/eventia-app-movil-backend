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
    private Integer available;
    private Integer total;
    private String tier;
    private String zone;
    private Boolean isPresale;
    private Integer maxPerPurchase;
    private LocalDate saleStartDate;
    private LocalDate saleEndDate;
    private Boolean active;

    // Métodos para compatibilidad total con Frontend (React Native / Stitch / JSON)
    public void setAvailable(Integer available) {
        this.available = available;
        if (this.availableCapacity == null) {
            this.availableCapacity = available;
        }
        if (this.totalCapacity == null) {
            this.totalCapacity = available;
        }
    }

    public Integer getAvailable() {
        if (available != null) return available;
        return availableCapacity != null ? availableCapacity : totalCapacity;
    }

    public void setTotal(Integer total) {
        this.total = total;
        if (this.totalCapacity == null) {
            this.totalCapacity = total;
        }
    }

    public Integer getTotal() {
        if (total != null) return total;
        return totalCapacity != null ? totalCapacity : availableCapacity;
    }
}
