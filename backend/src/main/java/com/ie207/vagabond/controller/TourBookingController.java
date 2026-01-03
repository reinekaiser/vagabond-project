package com.ie207.vagabond.controller;

import com.ie207.vagabond.model.HotelBooking;
import com.ie207.vagabond.model.TourBooking;
import com.ie207.vagabond.repository.TourBookingRepository;
import com.ie207.vagabond.service.TourBookingService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tourBooking")
@RequiredArgsConstructor
public class TourBookingController {
    private final TourBookingService tourBookingService;


    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMyTourBookings(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        List<Map<String, Object>> bookings = tourBookingService.getMyTourBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<TourBooking> cancelTourBooking(@PathVariable String id) {
        try {
            TourBooking booking = tourBookingService.cancelTourBooking(id);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error occurred: " + e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<Map<String, Object>> getTourBookings(
            @RequestParam(defaultValue = "all") String bookingStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        Map<String, Object> response = tourBookingService.getTourBookings(bookingStatus, page, limit);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TourBooking> updateTourBookingStatus(
            @PathVariable String id,
            @RequestBody String bookingStatus
    ) {
        try {
            TourBooking booking = tourBookingService.updateTourBookingStatus(id, bookingStatus);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error occurred: " + e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
