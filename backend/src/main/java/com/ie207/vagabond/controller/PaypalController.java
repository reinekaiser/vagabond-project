package com.ie207.vagabond.controller;

import com.ie207.vagabond.model.HotelBooking;
import com.ie207.vagabond.model.TourBooking;
import com.ie207.vagabond.request.HotelBookingRequest;
import com.ie207.vagabond.request.HotelPaypalOrderRequest;
import com.ie207.vagabond.request.TourBookingRequest;
import com.ie207.vagabond.request.TourPayPalOrderRequest;
import com.ie207.vagabond.service.PaypalService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
@RequiredArgsConstructor
public class PaypalController {
    private final PaypalService paypalService;

    @PostMapping("/create-hotel-booking")
    public ResponseEntity<?> createHotelOrder(@RequestBody HotelPaypalOrderRequest paypalOrder) {
        try {
            String approvalUrl = paypalService.createHotelPaypalOrder(paypalOrder);
            return ResponseEntity.ok(Map.of("approvalUrl", approvalUrl));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi tạo PayPal order"));
        }
    }

    @PostMapping("/capture-hotel-booking")
    public ResponseEntity<?> captureHotelOrder(
            @RequestBody HotelBookingRequest request
    ) {
        try {
            HotelBooking booking = paypalService.captureHotelPaypalOrder(
                    request
            );
            Map<String, Object> response = new HashMap<>();
            response.put("order", booking);
            if ("pending".equals(booking.getBookingStatus())) {
                response.put("message", "Đã thanh toán và lưu đơn hàng");
            }
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/create-tour-booking")
    public ResponseEntity<?> createTourOrder(@RequestBody TourPayPalOrderRequest paypalOrder) {
        try {
            String approvalUrl = paypalService.createTourPaypalOrder(paypalOrder);
            return ResponseEntity.ok(Map.of("approvalUrl", approvalUrl));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi tạo PayPal order"));
        }
    }

    @PostMapping("/capture-tour-booking")
    public ResponseEntity<?> captureTourOrder(@RequestBody TourBookingRequest bookingData){
        try {
            TourBooking booking = paypalService.captureTourPaypalOrder(
                    bookingData
            );
            Map<String, Object> response = new HashMap<>();
            response.put("order", booking);
            if ("pending".equals(booking.getBookingStatus())) {
                response.put("message", "Đã thanh toán và lưu đơn hàng");
            }
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
