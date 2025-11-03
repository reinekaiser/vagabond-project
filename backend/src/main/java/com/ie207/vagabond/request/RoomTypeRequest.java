package com.ie207.vagabond.request;

import com.ie207.vagabond.model.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomTypeRequest {
    private String name;
    private List<String> img = new ArrayList<>();
    private String area;
    private String view;
    private List<String> roomFacilities = new ArrayList<>();
    private List<Room> rooms;
}
