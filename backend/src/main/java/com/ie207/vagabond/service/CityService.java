package com.ie207.vagabond.service;

import com.ie207.vagabond.model.City;
import com.ie207.vagabond.model.Hotel;
import com.ie207.vagabond.repository.CityRepository;
import com.ie207.vagabond.repository.HotelRepository;
import com.ie207.vagabond.response.HotelSearchSuggestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.text.Normalizer;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {
    private final CityRepository cityRepository;
    private final HotelRepository hotelRepository;

    public List<City> getAll() {
        return cityRepository.findAll();
    }

    public List<City> getSearchHotelSuggestionsByCityName(String cityName) {
        String normalized = removeAccents(cityName).toLowerCase();
        List<City> allCities = cityRepository.findAll();
        return allCities.stream()
                .filter(city -> {
                    String cityNameNormalized = removeAccents(city.getName()).toLowerCase();
                    String cityNameLower = city.getName().toLowerCase();
                    return cityNameLower.contains(cityName.toLowerCase()) ||
                            cityNameNormalized.contains(normalized);
                })
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
    }

    public List<Hotel> getSearchHotelSuggestionsByHotelName(String hotelName) {
        String normalized = removeAccents(hotelName).toLowerCase();
        List<Hotel> allHotels = hotelRepository.findAll();
        return allHotels.stream()
                .filter( hotel -> {
                    String hotelNameNormalized = removeAccents(hotel.getName()).toLowerCase();
                    String hotelNameLower = hotel.getName().toLowerCase();
                    return hotelNameLower.contains(hotelName.toLowerCase()) ||
                            hotelNameNormalized.contains(normalized);
                })
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
    }

    public HotelSearchSuggestionResponse getSearchHotelSuggestion(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new HotelSearchSuggestionResponse(List.of(), List.of());
        }
        List<City> matchedCities = getSearchHotelSuggestionsByCityName(keyword);
        List<Hotel> matchedHotels = getSearchHotelSuggestionsByHotelName(keyword);

        HotelSearchSuggestionResponse response = new HotelSearchSuggestionResponse();
        response.setCities(matchedCities);
        response.setHotels(matchedHotels);

        return response;
    }


    private String removeAccents(String input) {
        if (input == null) return null;
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", ""); // xóa dấu
    }
}
