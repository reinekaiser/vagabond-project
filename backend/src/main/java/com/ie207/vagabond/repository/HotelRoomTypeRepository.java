package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.HotelRoomType;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HotelRoomTypeRepository extends MongoRepository<HotelRoomType, String> {
}
