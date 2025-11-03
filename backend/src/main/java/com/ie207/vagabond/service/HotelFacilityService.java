package com.ie207.vagabond.service;

import com.ie207.vagabond.model.HotelCategory;
import com.ie207.vagabond.model.HotelFacility;
import com.ie207.vagabond.repository.HotelCategoryRepository;
import com.ie207.vagabond.repository.HotelFacilityRepository;
import com.ie207.vagabond.response.GroupedFacilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelFacilityService {
    private final HotelFacilityRepository hotelFacilityRepository;
    private final HotelCategoryRepository hotelCategoryRepository;

    public List<GroupedFacilityResponse> groupFacilityByCategory() {
        Map<String, List<HotelFacility>> facilityByCategory = hotelFacilityRepository.findAll().stream()
                .collect(Collectors.groupingBy(f -> f.getCategory().get_id()));

        return hotelCategoryRepository.findAll().stream()
                .map(category -> new GroupedFacilityResponse(
                        category.get_id(),
                        category.getName(),
                        facilityByCategory.getOrDefault(category.get_id(), List.of())
                                .stream()
                                .map(f -> new GroupedFacilityResponse.FacilityItem(
                                                f.get_id(), f.getName())
                                )
                                .toList()
                ))
                .toList();
    }


}
