package com.ie207.vagabond.controller;


import com.ie207.vagabond.model.HotelBooking;
import com.ie207.vagabond.model.TourBooking;
import com.ie207.vagabond.request.HotelBookingRequest;
import com.ie207.vagabond.request.HotelPaypalOrderRequest;
import com.ie207.vagabond.request.TourBookingRequest;
import com.ie207.vagabond.request.TourPayPalOrderRequest;
import com.ie207.vagabond.service.VnpayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
public class VnpayController {
    private final VnpayService vnpayService;

    @PostMapping("/create-hotel-order")
    public ResponseEntity<?> createHotelVnpayOrder(@RequestBody HotelPaypalOrderRequest request) {
        try {
            String vnpayUrl = vnpayService.createHotelVnpayOrder(request);
            return ResponseEntity.ok().body(Map.of("url", vnpayUrl));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/capture-hotel-booking")
    public ResponseEntity<?> captureHotelOrder(
            @RequestParam Map<String, String> allParams,
            @RequestBody HotelBookingRequest request
    ) {
        try {
            String txnRef = allParams.get("vnp_TxnRef");
            String transactionStatus = allParams.get("vnp_TransactionStatus");
            String responseCode = allParams.get("vnp_ResponseCode");

            Map<String, Object> response = new HashMap<>();

            if ("00".equals(transactionStatus) && "00".equals(responseCode)) {
                HotelBooking booking = vnpayService.captureHotelVnpayOrder(request);

                response.put("order", booking);
                if ("pending".equals(booking.getBookingStatus())) {
                    response.put("message", "Đã thanh toán và lưu đơn hàng");
                }
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                String url = vnpayService.createCancelUrl(request);
                response.put("url", url);
                response.put("success", false);
                return ResponseEntity.ok(response);
            }
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/create-tour-order")
    public ResponseEntity<?> createTourVnpayOrder(@RequestBody TourPayPalOrderRequest request) {
        try {
            String vnpayUrl = vnpayService.createTourVnpayOrder(request);
            return ResponseEntity.ok().body(Map.of("url", vnpayUrl));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/capture-tour-booking")
    public ResponseEntity<?> captureTourOrder(
            @RequestParam Map<String, String> allParams,
            @RequestBody TourBookingRequest request
    ) {
        try {
            String txnRef = allParams.get("vnp_TxnRef");
            String transactionStatus = allParams.get("vnp_TransactionStatus");
            String responseCode = allParams.get("vnp_ResponseCode");

            Map<String, Object> response = new HashMap<>();

            if ("00".equals(transactionStatus) && "00".equals(responseCode)) {
                TourBooking booking = vnpayService.captureTourVnpayOrder(request);

                response.put("order", booking);
                if ("pending".equals(booking.getBookingStatus())) {
                    response.put("message", "Đã thanh toán và lưu đơn hàng");
                }
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                String url = vnpayService.createTourCancelUrl(request);
                response.put("url", url);
                response.put("success", false);
                return ResponseEntity.ok(response);
            }
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

}
