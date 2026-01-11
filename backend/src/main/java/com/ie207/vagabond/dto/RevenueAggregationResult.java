package com.ie207.vagabond.dto;
import lombok.Data;

@Data
public class RevenueAggregationResult {
    private DateId _id;
    private double revenue;
    private int bookings;

    @Data
    public static class DateId {
        private int year;
        private int month;
        private int day;

        public String toDateString() {
            return String.format("%d-%02d-%02d", year, month, day);
        }
    }
}
