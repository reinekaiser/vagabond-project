package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.City;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CityRepository extends MongoRepository<City,String> {
}
