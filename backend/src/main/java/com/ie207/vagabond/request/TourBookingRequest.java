package com.ie207.vagabond.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourBookingRequest {
    private String orderID;

    private String userId;
    private String tourId;
    private String ticketId;

    private String tourImg;
    private String name;
    private String email;
    private String phone;
    private String useDate;

    private String paymentMethod;
    private Double totalPrice;
    private String bookingStatus;

}
