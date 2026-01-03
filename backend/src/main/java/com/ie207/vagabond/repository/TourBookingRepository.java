package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.TourBooking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TourBookingRepository extends MongoRepository<TourBooking, String> {
    List<TourBooking> findByUserIdOrderByCreatedAtDesc(String userId);

}
