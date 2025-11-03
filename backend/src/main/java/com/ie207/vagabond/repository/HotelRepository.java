package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.Hotel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HotelRepository extends MongoRepository<Hotel, String> {
}
