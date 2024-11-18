package com.project.ssi_wypozyczalnia.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private int id;
    private int userId;
    private int bikeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalPrice;
}
