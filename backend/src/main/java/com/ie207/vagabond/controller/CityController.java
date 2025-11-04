package com.ie207.vagabond.controller;

import com.ie207.vagabond.model.City;
import com.ie207.vagabond.request.CityRequest;
import com.ie207.vagabond.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/")
    public ResponseEntity<City> createCity(@RequestBody CityRequest request) {
        City city = cityService.createCity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(city);
    }

    @PutMapping("/{cityId}")
    public ResponseEntity<City> updateCity(@RequestBody CityRequest request, @PathVariable String cityId) {
        City city = cityService.updateCity(cityId, request);
        return ResponseEntity.ok(city);
    }

    @DeleteMapping("/{cityId}")
    public ResponseEntity<?> deleteCity(@PathVariable String cityId) {
        try {
            cityService.deleteCity(cityId);
            return ResponseEntity.ok(Map.of("message", "City deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
