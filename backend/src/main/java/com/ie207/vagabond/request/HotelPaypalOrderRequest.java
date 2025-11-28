package com.ie207.vagabond.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelPaypalOrderRequest {
    private Double amount;
    private String hotelId;
    private String location;
    private String checkIn;
    private String checkOut;
    private Integer rooms;
    private Integer adults;
    private String roomTypeId;
    private String roomId;
}
