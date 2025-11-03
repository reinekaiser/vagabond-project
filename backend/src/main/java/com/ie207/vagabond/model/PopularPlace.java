package com.ie207.vagabond.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PopularPlace {
    @Id
    private String _id;          // ánh xạ từ _id
    private String name;
    private String description = "";
    private String img = "";
}
