package com.ie207.vagabond.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "hotelbookings")
@Builder
public class HotelBooking {
    @Id
    private String _id;
    private String userId;
    private String hotelId;
    private String roomTypeId;
    private String roomId;

    private String name;
    private String email;
    private String phone;
    private LocalDate checkin;
    private LocalDate checkout;
    private int numGuests;
    private int numRooms;

    private String paymentMethod;
    private double totalPrice;
    private String bookingStatus = "pending"; // failed, pending, confirmed, canceled
    private Boolean isReviewed = false;

    @CreatedDate
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;

}
