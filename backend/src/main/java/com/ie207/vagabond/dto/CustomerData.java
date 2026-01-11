package com.ie207.vagabond.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerData {
    private String userId;
    private int totalBookings;
    private double totalSpent;

    public void addBookings(int bookings) {
        this.totalBookings += bookings;
    }

    public void addSpent(double spent) {
        this.totalSpent += spent;
    }
}