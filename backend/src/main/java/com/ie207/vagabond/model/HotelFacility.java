package com.ie207.vagabond.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "hotelfacilities")
public class HotelFacility {
    @Id
    private String _id;
    @DBRef
    private HotelCategory category;
    private String name;
}
