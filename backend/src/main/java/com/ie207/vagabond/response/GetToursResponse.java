package com.ie207.vagabond.response;

import com.ie207.vagabond.model.Tour;

import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetToursResponse {
    private Long totalTours;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
    private List<Tour> data;
}
