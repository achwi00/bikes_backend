package com.project.ssi_wypozyczalnia.dao;

import com.project.ssi_wypozyczalnia.entity.Favourite;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FavouriteDAO {
    private final Connection connection;

    public FavouriteDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Favourite> getAllFavouritesForUser(int userId) throws SQLException {
        String sql = "SELECT * FROM favourite WHERE user_id = ?";
        List<Favourite> favourites = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    favourites.add(mapResultSetToFavourite(rs));
                }
            }
        }
        return favourites;
    }

    public void addFavourite(int userId, int bikeId) throws SQLException {
        String sql = "INSERT INTO favourite (user_id, bike_id, added_at) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bikeId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now())); // Aktualny czas
            stmt.executeUpdate();
        }
    }

    public boolean removeFavourite(int userId, int bikeId) throws SQLException {
        String sql = "DELETE FROM favourite WHERE user_id = ? AND bike_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bikeId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Zwraca true, jeśli usunięto co najmniej jeden wiersz
        }
    }

    private Favourite mapResultSetToFavourite(ResultSet rs) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return new Favourite(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("bike_id"),
                rs.getTimestamp("added_at").toLocalDateTime().format(formatter)
        );
    }
}
