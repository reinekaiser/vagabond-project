package com.ie207.vagabond.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitySuggestion {
    private String _id;
    private String name;
}