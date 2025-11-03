package com.ie207.vagabond.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupedFacilityResponse {
    private String value;          // ID của category
    private String title;          // Tên category
    private List<FacilityItem> children; // Danh sách facility thuộc category

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FacilityItem {
        private String value;  // ID của facility
        private String title;  // Tên facility
    }
}
