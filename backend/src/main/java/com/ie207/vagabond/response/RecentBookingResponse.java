package com.ie207.vagabond.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentBookingResponse {
    private String _id;
    private String type;
    private String customerName;
    private String customerEmail;
    private String serviceName;
    private String serviceLocation;
    private double totalPrice;
    private String bookingStatus;
    private Date createdAt;
}
