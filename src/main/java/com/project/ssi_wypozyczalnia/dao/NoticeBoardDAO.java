package com.project.ssi_wypozyczalnia.dao;

import com.project.ssi_wypozyczalnia.entity.NoticeBoard;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoticeBoardDAO {
    private final Connection connection;

    public NoticeBoardDAO(Connection connection) {
        this.connection = connection;
    }

    public void addNoticeBoard(NoticeBoard notice) throws SQLException {
        String sql = "INSERT INTO notice_board (title, content, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, notice.getTitle());
            stmt.setString(2, notice.getContent());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now())); // Aktualny czas
            stmt.executeUpdate();
        }
    }

    public List<NoticeBoard> getAllNoticeBoardsSorted() throws SQLException {
        String sql = "SELECT * FROM notice_board ORDER BY created_at DESC";
        List<NoticeBoard> noticeBoards = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                noticeBoards.add(mapResultSetToNoticeBoard(rs));
            }
        }
        return noticeBoards;
    }

    public NoticeBoard getNoticeBoardById(int id) throws SQLException {
        String sql = "SELECT * FROM notice_board WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNoticeBoard(rs);
                }
            }
        }
        return null;
    }

    private NoticeBoard mapResultSetToNoticeBoard(ResultSet rs) throws SQLException {
        return new NoticeBoard(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}

