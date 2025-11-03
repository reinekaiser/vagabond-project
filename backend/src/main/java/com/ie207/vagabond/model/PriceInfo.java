package com.ie207.vagabond.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceInfo {
    private String priceType;
    private String notes;
    private Integer price;
    private Integer minPerBooking;
    private Integer maxPerBooking;
}