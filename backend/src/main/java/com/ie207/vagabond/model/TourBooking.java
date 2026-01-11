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
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tourbookings")
public class TourBooking {
    @Id
    private String _id;
    private String userId;
    private String tourId;
    private String ticketId;

    private String tourImg;
    private String name;
    private String email;
    private String phone;
    private LocalDate useDate;

    private String paymentMethod;
    private double totalPrice;
    private String bookingStatus = "pending";
    private Boolean isReviewed = false;

    @CreatedDate
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;

}
