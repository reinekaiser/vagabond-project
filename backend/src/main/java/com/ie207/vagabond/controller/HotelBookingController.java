package com.ie207.vagabond.controller;

import com.ie207.vagabond.model.HotelBooking;
import com.ie207.vagabond.request.HotelBookingRequest;
import com.ie207.vagabond.service.HotelBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hotelBooking")
public class HotelBookingController {
    private final HotelBookingService hotelBookingService;

    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMyBookings(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        List<Map<String, Object>> bookings = hotelBookingService.getMyBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/bookings")
    public ResponseEntity<Map<String, Object>> getHotelBookings(
            @RequestParam(defaultValue = "all") String bookingStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        Map<String, Object> response = hotelBookingService.getHotelBookings(bookingStatus, page, limit);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<HotelBooking> updateHotelBookingStatus(
            @PathVariable String id,
            @RequestBody String bookingStatus
    ) {
        try {
            HotelBooking booking = hotelBookingService.updateHotelBookingStatus(id, bookingStatus);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error occurred: " + e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<HotelBooking> cancelHotelBooking(@PathVariable String id) {
        try {
            HotelBooking booking = hotelBookingService.cancelHotelBooking(id);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error occurred: " + e);
            return ResponseEntity.status(500).body(null);
        }
    }
}

