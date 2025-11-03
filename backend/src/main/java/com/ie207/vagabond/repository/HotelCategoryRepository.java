package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.HotelCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HotelCategoryRepository extends MongoRepository<HotelCategory, String> {
}
