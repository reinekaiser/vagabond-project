package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.HotelFacility;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HotelFacilityRepository extends MongoRepository<HotelFacility, String> {
}
