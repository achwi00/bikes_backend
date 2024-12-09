package com.project.ssi_wypozyczalnia.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    private int id;
    private String username;
    private String surname;
    private String email;
    private String passwordHash;
    private String role;
    private Boolean isBlocked;
}


