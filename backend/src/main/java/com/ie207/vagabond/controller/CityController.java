package com.ie207.vagabond.controller;

import com.ie207.vagabond.model.City;
import com.ie207.vagabond.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cities")
public class CityController {
    private final CityService cityService;

    @GetMapping("/")
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<City>> getSearchHotelSuggestionsByCityName(
            @RequestParam String key
    ) {
        List<City> cities = cityService.getSearchHotelSuggestionsByCityName(key);
        return ResponseEntity.ok(cities);
    }
}
