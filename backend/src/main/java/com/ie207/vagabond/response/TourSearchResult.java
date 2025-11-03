package com.ie207.vagabond.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourSearchResult {
    private String id;
    private String name;
    private String location;
    private List<String> images;
    private List<String> category;
    private List<String> languageService;
    private String duration;
    private Integer fromPrice;
}