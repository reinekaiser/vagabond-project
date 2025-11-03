package com.ie207.vagabond.request;

import com.ie207.vagabond.model.Policies;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelRequest {
    private String name;
    private String description;
    private String address;
    private String cityId;
    private double lat;
    private double lng;
    private int rooms;

    private List<String> serviceFacilities;
    private Policies policies = new Policies();
    private List<String> img;
    private List<RoomTypeRequest> roomTypes;



}
