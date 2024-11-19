package com.project.ssi_wypozyczalnia.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeBoard {
    private int id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}
