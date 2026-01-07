package com.ie207.vagabond.response;

import com.ie207.vagabond.model.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelFilterResponse {
    private int totalHotels;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private List<Document> data;
}
