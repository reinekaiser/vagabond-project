package com.ie207.vagabond.request;

import com.ie207.vagabond.model.PopularPlace;
import com.ie207.vagabond.model.PopularQuestion;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CityRequest {
    private String name;
    private String description;
    private String bestTimeToVisit;
    private List<PopularPlace> popularPlaces;
    private List<String> images;
    private List<PopularQuestion> popularQuestions;
}
