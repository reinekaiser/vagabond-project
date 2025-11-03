package com.ie207.vagabond.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourStatsResponse {
    private long totalCount;
    private List<TourSummary> topRatedTours;
    private List<TourSummary> cheapestTours;
    private List<TourSummary> newestTours;
}

