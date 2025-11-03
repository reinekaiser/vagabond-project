package com.ie207.vagabond.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateTourRequest {
    private String name;
    private List<String> category;
    private String location;
    private String duration;
    private String experiences;
    private List<String> images;
    private List<String> languageService;
    private String contact;
    private String suitableFor;
    private String additionalInformation;
    private String itinerary;
    private Integer fromPrice;
    private Double avgRating;
    private Integer bookings;
}
