package com.ie207.vagabond.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest {
    private double rating;
    private String comment;
    private List<String> images;
    private String userId;
    private String productType; //'Hotel', 'Tour'
    private String productId;
    private String bookingId;
}
