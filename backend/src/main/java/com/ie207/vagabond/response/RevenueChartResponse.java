package com.ie207.vagabond.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueChartResponse {
    private String date;
    private double tourRevenue;
    private int tourBookings;
    private double hotelRevenue;
    private int hotelBookings;

    public double getTotalRevenue() {
        return tourRevenue + hotelRevenue;
    }

    public int getTotalBookings() {
        return tourBookings + hotelBookings;
    }
}
