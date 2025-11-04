package com.ie207.vagabond.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourSummary {
    private String _id;
    private String name;
    private LocalDateTime createdAt; // Chỉ có trong newest tours
}
