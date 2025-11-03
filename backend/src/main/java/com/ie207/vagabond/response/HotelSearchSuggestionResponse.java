package com.ie207.vagabond.response;

import com.ie207.vagabond.model.City;
import com.ie207.vagabond.model.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelSearchSuggestionResponse {
    private List<City> cities;
    private List<Hotel> hotels;
}
