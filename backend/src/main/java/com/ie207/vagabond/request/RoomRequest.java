package com.ie207.vagabond.request;

import com.ie207.vagabond.model.CancellationPolicy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {
    private String bedType;
    private String serveBreakfast;
    private int maxOfGuest;
    private int numberOfRoom;
    private CancellationPolicy cancellationPolicy;
    private double price;
}
