package com.ie207.vagabond.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import org.bson.types.ObjectId;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Room {
    @Id
    private String _id;
    private String bedType;
    private String serveBreakfast;
    private int maxOfGuest;
    private int numberOfRoom;
    private CancellationPolicy cancellationPolicy = new CancellationPolicy();
    private double price;

    @CreatedDate
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;

    public Room(String bedType, String serveBreakfast, int maxOfGuest, int numberOfRoom, double price, CancellationPolicy cancellationPolicy) {
        this._id = new ObjectId().toString();

        this.bedType = bedType;
        this.serveBreakfast = serveBreakfast;
        this.maxOfGuest = maxOfGuest;
        this.numberOfRoom = numberOfRoom;
        this.price = price;
        this.cancellationPolicy = cancellationPolicy;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

}
