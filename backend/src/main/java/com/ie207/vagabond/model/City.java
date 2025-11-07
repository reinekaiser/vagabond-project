package com.ie207.vagabond.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "cities")
@Builder
public class City {
    @Id
    private String _id;
    private String name;
    private String description;
    private String bestTimeToVisit;

    private List<PopularPlace> popularPlaces = new ArrayList<>();
    private List<String> images = new ArrayList<>();
    private List<PopularQuestion> popularQuestions = new ArrayList<>();

    @CreatedDate
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;
}
