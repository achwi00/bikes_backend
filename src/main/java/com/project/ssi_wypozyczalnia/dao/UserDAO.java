package com.project.ssi_wypozyczalnia.dao;

import com.project.ssi_wypozyczalnia.entity.Users;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public void addUser(Users users) throws SQLException {
        String sql = "INSERT INTO users (name, surname, email, password_hash, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, users.getUsername());
            stmt.setString(2, users.getSurname());
            stmt.setString(3, users.getEmail());
            stmt.setString(4, users.getPasswordHash());
            stmt.setString(5, users.getRole());
            stmt.executeUpdate();
        }
    }

    public Users getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Users(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("surname"),
                        rs.getString("email"),
                        rs.getString("passwordhash"),
                        rs.getString("userrole")
                );
            }
        }
        return null;
    }

    public boolean updateUser(Users users) throws SQLException {
        String sql = "UPDATE users SET name = ?, surname = ?, email = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, users.getUsername());
            stmt.setString(2, users.getSurname());
            stmt.setString(3, users.getEmail());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0; // Zwraca true, jeśli user został zaktualizowany
        }
    }

}

