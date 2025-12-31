package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.TourReview;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TourReviewRepository extends MongoRepository<TourReview, String> {
    List<TourReview> findByTourId(String tourId);
}
