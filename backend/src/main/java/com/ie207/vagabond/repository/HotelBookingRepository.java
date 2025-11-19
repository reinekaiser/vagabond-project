package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.HotelBooking;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HotelBookingRepository extends MongoRepository<HotelBooking, String> {
}
