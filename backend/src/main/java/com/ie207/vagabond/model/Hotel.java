package com.ie207.vagabond.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "hotels")
@Builder
public class Hotel {
    @Id
    private String _id;
    private String name;
    private List<String> img = new ArrayList<>();
    private double lat = 0.0;
    private double lng = 0.0;
    private String address = "";

    @DBRef
    private City city;

    private int rooms = 0;
    private double averageRating = 0;
    private String description = "";

    @DBRef
    private List<HotelFacility> serviceFacilities = new ArrayList<>();

    private Policies policies = new Policies();

    @DBRef
    private List<HotelRoomType> roomTypes = new ArrayList<>();

    private double fromPrice = 0.0;

    @CreatedDate
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;
}
