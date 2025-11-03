package com.ie207.vagabond.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "cities")
public class City {
    @Id
    private String id;
    private String name;
    private String description;
    private String bestTimeToVisit;
    private List<PopularPlace> popularPlaces;
    private String img;
    private List<PopularQuestion> popularQuestions;
}
