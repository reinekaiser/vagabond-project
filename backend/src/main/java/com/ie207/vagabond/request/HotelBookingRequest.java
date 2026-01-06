package com.ie207.vagabond.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelBookingRequest {
    private String orderID;

    private String userId;
    private String hotelId;
    private String roomTypeId;
    private String roomId;

    private String name;
    private String email;
    private String phone;

    private String checkin;
    private String checkout;

    private Integer numGuests;
    private Integer numRooms;

    private String paymentMethod;
    private Double totalPrice;
    private String bookingStatus;
}
