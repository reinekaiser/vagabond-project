package com.ie207.vagabond.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Policies {
    private String timeCheckin = "";
    private String timeCheckout = "";
    private String checkinPolicy = "";
    private String childrenPolicy = "";
    private String mandatoryFees = "";
    private String otherFees = "";
    private String FoodDrinks = "";
    private String allowPet = "";
}
