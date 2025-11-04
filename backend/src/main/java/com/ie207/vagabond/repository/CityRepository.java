package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.City;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends MongoRepository<City, String> {
    Optional<City> findByName(String cityName);
    boolean existsByName(String cityName);
}
