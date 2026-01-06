package com.ie207.vagabond.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourPayPalOrderRequest {
    private Double amount;
    private String tourId;
}
