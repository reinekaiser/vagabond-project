package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.Tour;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TourRepository extends MongoRepository<Tour,String> {
    long count();

    List<Tour> findTop3ByOrderByAvgRatingDesc();

    List<Tour> findTop3ByOrderByFromPriceAsc();

    List<Tour> findTop3ByOrderByCreatedAtDesc();
}
