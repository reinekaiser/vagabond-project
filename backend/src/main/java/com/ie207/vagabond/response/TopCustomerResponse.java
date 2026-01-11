package com.ie207.vagabond.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomerResponse {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String avatar;
    private int totalBookings;
    private double totalSpent;
}
