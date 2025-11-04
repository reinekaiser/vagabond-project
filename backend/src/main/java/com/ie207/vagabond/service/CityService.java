package com.ie207.vagabond.service;

import com.ie207.vagabond.model.City;
import com.ie207.vagabond.model.Hotel;
import com.ie207.vagabond.model.PopularPlace;
import com.ie207.vagabond.repository.CityRepository;
import com.ie207.vagabond.repository.HotelRepository;
import com.ie207.vagabond.request.CityRequest;
import com.ie207.vagabond.response.HotelSearchSuggestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {
    private final CityRepository cityRepository;
    private final HotelRepository hotelRepository;
    private final CloudinaryService cloudinaryService;
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

    public City createCity(CityRequest request) {
        if (cityRepository.existsByName(request.getName())) {
            throw new RuntimeException("City with name '" + request.getName() + "' already exists");
        }

        City city = new City();
        city.setName(request.getName());
        city.setDescription(request.getDescription());
        city.setBestTimeToVisit(request.getBestTimeToVisit());

        city.setPopularPlaces(request.getPopularPlaces() != null ?
                request.getPopularPlaces() : new ArrayList<>());
        city.setImages(request.getImages() != null ?
                request.getImages() : new ArrayList<>());
        city.setPopularQuestions(request.getPopularQuestions() != null ?
                request.getPopularQuestions() : new ArrayList<>());

        return cityRepository.save(city);
    }

    public City updateCity(String cityId, CityRequest request) {
        City existingCity = cityRepository.findById(cityId).orElseThrow(() -> new RuntimeException("City with id '" + cityId + "' not found"));

        if (request.getName() != null && !request.getName().equals(existingCity.getName())) {
            if (cityRepository.existsByName(request.getName())) {
                throw new RuntimeException("City with name '" + request.getName() + "' already exists");
            }
            existingCity.setName(request.getName());
        }

        if (request.getDescription() != null) {
            existingCity.setDescription(request.getDescription());
        }
        if (request.getBestTimeToVisit() != null) {
            existingCity.setBestTimeToVisit(request.getBestTimeToVisit());
        }
        if (request.getPopularPlaces() != null) {
            Set<String> oldImages = existingCity.getPopularPlaces().stream()
                    .map(PopularPlace::getImage)
                    .filter(Objects::nonNull)
                    .filter(img -> !img.isEmpty())
                    .collect(Collectors.toSet());

            Set<String> newImages = request.getPopularPlaces()
                    .stream()
                    .map(PopularPlace::getImage)
                    .filter(Objects::nonNull)
                    .filter(img -> !img.isEmpty())
                    .collect(Collectors.toSet());

            Set<String> imagesToDelete = oldImages.stream().filter(img -> !newImages.contains(img)).collect(Collectors.toSet());
            if (!imagesToDelete.isEmpty()) {
                cloudinaryService.deleteImagesBatch(new ArrayList<>(imagesToDelete));
            }

            existingCity.setPopularPlaces(request.getPopularPlaces());
        }
        if (request.getPopularQuestions() != null) {
            existingCity.setPopularQuestions(request.getPopularQuestions());
        }
        if (request.getImages() != null) {
            Set<String> oldImages = new HashSet<>(existingCity.getImages());
            Set<String> newImages = new HashSet<>(request.getImages());

            Set<String> imagesToDelete = oldImages.stream()
                    .filter(oldImg -> !newImages.contains(oldImg))
                    .collect(Collectors.toSet());

            if (!imagesToDelete.isEmpty()) {
                System.out.println("Deleting old city images: " + imagesToDelete);
                cloudinaryService.deleteImagesBatch(new ArrayList<>(imagesToDelete));
            }

            existingCity.setImages(request.getImages());
        }

        return cityRepository.save(existingCity);
    }

    @Transactional
    public void deleteCity(String cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + cityId));

        List<String> allImagesToDelete = new ArrayList<>();

        if (city.getImages() != null) {
            allImagesToDelete.addAll(city.getImages());
        }

        // Thêm ảnh từ popularPlace
        if (city.getPopularPlaces() != null) {
            city.getPopularPlaces().stream()
                    .map(PopularPlace::getImage)
                    .filter(Objects::nonNull)
                    .filter(img -> !img.isEmpty())
                    .forEach(allImagesToDelete::add);
        }

        if (!allImagesToDelete.isEmpty()) {
            cloudinaryService.deleteImagesBatch(allImagesToDelete);
        }

        cityRepository.delete(city);
    }
}
