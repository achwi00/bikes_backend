package com.project.ssi_wypozyczalnia.dao;

import com.project.ssi_wypozyczalnia.entity.Bike;
import com.project.ssi_wypozyczalnia.entity.BikeSize;
import com.project.ssi_wypozyczalnia.entity.BikeType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BikeDAO {
    private final Connection connection;

    public BikeDAO(Connection connection) {
        this.connection = connection;
    }

    public void addBike(Bike bike) throws SQLException {
        String sql = "INSERT INTO bike (name, type, size, available, price_per_day, description, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, bike.getName());
            stmt.setString(2, bike.getType().name());
            stmt.setString(3, bike.getSize().name());
            stmt.setBoolean(4, bike.isAvailable());
            stmt.setDouble(5, bike.getPricePerDay());
            stmt.setString(6, bike.getDescription());
            stmt.setString(7, bike.getImageUrl());
            stmt.executeUpdate();
        }
    }

    public Bike getBikeById(int id) throws SQLException {
        String sql = "SELECT * FROM bike WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Bike(
                        rs.getInt("id"),
                        rs.getString("name"),
                        BikeType.valueOf(rs.getString("type")),
                        BikeSize.valueOf(rs.getString("size")),
                        rs.getBoolean("available"),
                        rs.getDouble("price_per_day"),
                        rs.getString("description"),
                        rs.getString("image_url")
                );
            }
        }
        return null;
    }

    public List<Bike> getAllBikes() throws SQLException {
        String sql = "SELECT * FROM bike";
        List<Bike> bikes = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                bikes.add(mapResultSetToBike(rs));
            }
        }
        return bikes;
    }

    public List<Bike> searchBikes(BikeSize size, BikeType type, double maxPrice) throws SQLException {
        String sql = "SELECT * FROM bike WHERE size = ? AND type = ? AND price_per_day <= ?";
        List<Bike> bikes = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, size.name());
            stmt.setString(2, type.name());
            stmt.setDouble(3, maxPrice);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bikes.add(mapResultSetToBike(rs));
                }
            }
        }
        return bikes;
    }

    public boolean updateBike(Bike bike) throws SQLException {
        String sql = "UPDATE bike SET name = ?, type = ?, size = ?, available = ?, price_per_day = ?, description = ?, image_url = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, bike.getName());
            stmt.setString(2, bike.getType().name());
            stmt.setString(3, bike.getSize().name());
            stmt.setBoolean(4, bike.isAvailable());
            stmt.setDouble(5, bike.getPricePerDay());
            stmt.setString(6, bike.getDescription());
            stmt.setString(7, bike.getImageUrl());
            stmt.setInt(8, bike.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0; // Zwraca true, jeśli rower został zaktualizowany
        }
    }

    public boolean toggleAvailability(int bikeId) throws SQLException {
        String sql = "UPDATE bike SET available = NOT available WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bikeId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteBike(int bikeId) throws SQLException {
        String sql = "DELETE FROM bike WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bikeId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    private Bike mapResultSetToBike(ResultSet rs) throws SQLException {
        return new Bike(
                rs.getInt("id"),
                rs.getString("name"),
                BikeType.valueOf(rs.getString("type")),
                BikeSize.valueOf(rs.getString("size")),
                rs.getBoolean("available"),
                rs.getDouble("price_per_day"),
                rs.getString("description"),
                rs.getString("image_url")
        );
    }

}
