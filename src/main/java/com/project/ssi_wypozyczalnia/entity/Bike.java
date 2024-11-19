package com.project.ssi_wypozyczalnia.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bike {
    private int id;
    private String bikeName;
    private BikeType bikeType;
    private BikeSize bikeSize;
    private boolean available;
    private double pricePerDay;
    private String description;
    private String imageUrl;
}
