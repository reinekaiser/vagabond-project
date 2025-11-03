package com.ie207.vagabond.request;

import lombok.Data;

@Data
public class PriceInfoRequest {
    private String priceType;
    private String notes;
    private Integer price;
    private Integer minPerBooking;
    private Integer maxPerBooking;
}
