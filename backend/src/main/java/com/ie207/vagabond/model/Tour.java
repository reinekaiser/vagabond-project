package com.ie207.vagabond.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "tours")
public class Tour {
    @Id
    private String id;
    private String name;
    private List<String> category;
    private String location;

    @DBRef
    private City city;

    private Double avgRating;
    private Double rating;
    private String duration;
    private Double durationInHours;
    private String experiences;
    private List<String> images;
    private List<String> languageService;
    private String contact;
    private String suitableFor;
    private String additionalInformation;
    private String itinerary;
    private Integer fromPrice;
    private Integer bookings = 0;
    @DBRef
    private List<Ticket> tickets;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void calculateDurationInHours() {
        this.durationInHours = convertDurationToHours(this.duration);
    }

    private Double convertDurationToHours(String duration) {
        if (duration == null) return 0.0;

        double hours = 0.0;

        java.util.regex.Pattern dayPattern = java.util.regex.Pattern.compile("(\\d+)\\s*ngày");
        java.util.regex.Matcher dayMatcher = dayPattern.matcher(duration);
        if (dayMatcher.find()) {
            hours += Integer.parseInt(dayMatcher.group(1)) * 24;
        }

        java.util.regex.Pattern hourPattern = java.util.regex.Pattern.compile("(\\d+)\\s*giờ");
        java.util.regex.Matcher hourMatcher = hourPattern.matcher(duration);
        if (hourMatcher.find()) {
            hours += Integer.parseInt(hourMatcher.group(1));
        }

        // Match minutes: "(\d+)\s*phút"
        java.util.regex.Pattern minutePattern = java.util.regex.Pattern.compile("(\\d+)\\s*phút");
        java.util.regex.Matcher minuteMatcher = minutePattern.matcher(duration);
        if (minuteMatcher.find()) {
            hours += Integer.parseInt(minuteMatcher.group(1)) / 60.0;
        }

        return hours;
    }
}
