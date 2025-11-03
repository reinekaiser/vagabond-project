package com.ie207.vagabond.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionResponse {
    private List<TourSuggestion> tours;
    private List<CitySuggestion> cities;
}