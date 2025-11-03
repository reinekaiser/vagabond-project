package com.ie207.vagabond.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateTourRequest {
    private String name;
    private List<String> category;
    private String location;
    private String cityId;
    private String duration;
    private String experiences;
    private List<String> languageService;
    private String contact;
    private String suitableFor;
    private String additionalInformation;
    private String itinerary;
    private List<CreateTicketRequest> tickets;
    private Integer fromPrice;
    private List<String> images;
}







