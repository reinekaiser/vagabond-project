package com.ie207.vagabond.controller;


import com.ie207.vagabond.request.ReviewRequest;
import com.ie207.vagabond.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/")
    public ResponseEntity<?> getReviewsByProduct(
            @RequestParam String productId,
            @RequestParam String productType
    ) {
        try {
            List<Document> reviews = reviewService.getReviewsByProduct(productId, productType);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request) {
        try {
            Object savedReview = reviewService.createReview(request);
            Map<String, Object> response = new HashMap<>();
            response.put("review", savedReview);
            response.put("productType", request.getProductType());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable String id,
                                          @RequestBody ReviewRequest review) {
        try {
            Object updatedReview = reviewService.updateReview(id, review);

            Map<String, Object> response = new HashMap<>();
            response.put("review", updatedReview);
            response.put("productType", review.getProductType());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable String id) {
        try {
            String message = reviewService.deleteReview(id);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error deleting images");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<?> getMyReviews(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("Missing userId");
        }

        try {
            List<Document> reviews = reviewService.getMyReviews(userId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }

    @GetMapping("/order-can-review")
    public ResponseEntity<?> getOrdersCanReview(@RequestParam String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("Missing userId");
        }

        try {
            List<Document> bookings = reviewService.getHotelOrdersCanReview(userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }

    @GetMapping("/tour-order-can-review")
    public ResponseEntity<?> getTourOrdersCanReview(@RequestParam String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("Missing userId");
        }

        try {
            List<Document> bookings = reviewService.getTourOrdersCanReview(userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }

    @GetMapping("/review-city")
    public ResponseEntity<?> getReviewsByCity(@RequestParam String cityId) {
        if (cityId == null || cityId.isBlank()) {
            return ResponseEntity.badRequest().body("Missing cityId");
        }
        try {
            List<Document> reviews = reviewService.getReviewByCities(cityId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }

}
