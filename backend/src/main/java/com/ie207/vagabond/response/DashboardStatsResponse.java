package com.ie207.vagabond.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private double totalRevenue;
    private long tourBookingsCount;
    private long hotelBookingsCount;
    private long totalBookings;
    private long newUsersCount;
    private long totalUsers;
    private double completionRate;
    private double avgBookingValue;
}
