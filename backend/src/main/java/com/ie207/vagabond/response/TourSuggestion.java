package com.ie207.vagabond.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourSuggestion {
    private String id;
    private String name;
    private String location;
    private List<String> images;
    private List<TicketDetail> ticketDetails;
}
