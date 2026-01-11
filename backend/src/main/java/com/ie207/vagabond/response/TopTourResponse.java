package com.ie207.vagabond.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopTourResponse {
    private String _id;
    private String name;
    private String location;
    private List<String> images;
    private int totalBookings;
    private double totalRevenue;
}