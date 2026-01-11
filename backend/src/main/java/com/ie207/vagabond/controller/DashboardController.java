package com.ie207.vagabond.controller;

import com.ie207.vagabond.response.*;
import com.ie207.vagabond.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(@RequestParam(defaultValue = "7") int period) {
        DashboardStatsResponse dashboardStats = dashboardService.getDashboardStats(period);
        return ResponseEntity.ok(dashboardStats);
    }

    @GetMapping("/revenue-chart")
    public ResponseEntity<?> getRevenueChart(@RequestParam(defaultValue = "7") int period) {
        List<RevenueChartResponse> chartData = dashboardService.getRevenueChart(period);
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/top-tours")
    public ResponseEntity<?> getTopTours(@RequestParam(defaultValue = "5") int limit) {
        List<TopTourResponse> topTours = dashboardService.getTopTours(limit);
        return ResponseEntity.ok(topTours);
    }

    @GetMapping("/top-hotels")
    public ResponseEntity<?> getTopHotels(@RequestParam(defaultValue = "5") int limit) {
        List<TopHotelResponse> topTours = dashboardService.getTopHotels(limit);
        return ResponseEntity.ok(topTours);
    }

    @GetMapping("/recent-bookings")
    public ResponseEntity<?> getRecentBookings(@RequestParam(defaultValue = "10") int limit) {
        List<RecentBookingResponse>  recentBookings = dashboardService.getRecentBookings(limit);
        return ResponseEntity.ok(recentBookings);
    }

    @GetMapping("/top-customers")
    public ResponseEntity<?> getTopCustomers(
            @RequestParam(defaultValue = "5") int limit) {
        List<TopCustomerResponse> topCustomers = dashboardService.getTopCustomers(limit);
        return ResponseEntity.ok(topCustomers);
    }
}
