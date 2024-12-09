package com.project.ssi_wypozyczalnia.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favourite {
    private int id;
    private int userId;
    private int bikeId;
    private String addedAt;
}
