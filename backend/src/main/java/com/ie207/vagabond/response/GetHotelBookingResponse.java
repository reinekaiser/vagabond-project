package com.ie207.vagabond.response;

import com.ie207.vagabond.model.HotelBooking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetHotelBookingResponse {
    private List<HotelBooking> bookings;
    private long total;
    private int currentPage;
    private int pageSize;
    private int totalPages;
}
