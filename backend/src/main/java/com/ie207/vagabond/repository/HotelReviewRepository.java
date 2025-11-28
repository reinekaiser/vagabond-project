package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.HotelReview;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HotelReviewRepository extends MongoRepository<HotelReview, String> {
    List<HotelReview> findByHotelId(String hotelId);
}
