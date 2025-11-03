package com.ie207.vagabond.controller;

import com.ie207.vagabond.model.HotelFacility;
import com.ie207.vagabond.repository.HotelFacilityRepository;
import com.ie207.vagabond.response.GroupedFacilityResponse;
import com.ie207.vagabond.service.HotelFacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/facility")
@RequiredArgsConstructor
public class HotelFacilityController {
    private final HotelFacilityRepository hotelFacilityRepository;
    private final HotelFacilityService hotelFacilityService;

    @GetMapping("/")
    public ResponseEntity<List<HotelFacility>> listFacilities() {
        return ResponseEntity.ok(hotelFacilityRepository.findAll());
    }

    @GetMapping("/groupByCategory")
    public ResponseEntity<List<GroupedFacilityResponse>> groupFacilityByCategory(){
        return ResponseEntity.ok(hotelFacilityService.groupFacilityByCategory());
    }
}
