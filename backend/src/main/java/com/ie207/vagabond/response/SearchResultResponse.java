package com.ie207.vagabond.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultResponse {
    private List<TourSearchResult> tours;
    private int totalPages;
    private int page;
    private long total;
}