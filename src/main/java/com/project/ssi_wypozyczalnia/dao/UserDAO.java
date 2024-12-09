package com.project.ssi_wypozyczalnia.dao;

import com.project.ssi_wypozyczalnia.entity.Users;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    // Metoda do hashowania hasła
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Metoda do sprawdzania zgodności hasła
    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    public List<Users> getAllUsers() throws SQLException {
        List<Users> usersList = new ArrayList<>();
        String sql = "SELECT * FROM \"user\" ORDER BY id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Users user = new Users(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getBoolean("is_blocked")
                );
                usersList.add(user);
            }
        }

        return usersList;
    }

    public void addUser(Users users) throws SQLException {
        String sql = "INSERT INTO \"user\" (name, surname, email, password_hash, role) VALUES (?, ?, ?, ?, ?)";
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
        String sql = "SELECT * FROM \"user\" WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Users(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getBoolean("is_blocked")
                );
            }
        }
        return null;
    }

    public boolean updateUser(Users users) throws SQLException {
        String sql = "UPDATE \"user\" SET name = ?, surname = ?, email = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, users.getUsername());
            stmt.setString(2, users.getSurname());
            stmt.setString(3, users.getEmail());
            stmt.setInt(4, users.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0; // Zwraca true, jeśli user został zaktualizowany
        }
    }

    // Metoda do autoryzacji użytkownika
    public Users authenticateUser(String email, String password) throws SQLException {
        String sql = "SELECT * FROM \"user\" WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password_hash");
                if (checkPassword(password, hashedPassword)) {
                    return new Users(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("surname"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getBoolean("is_blocked")
                            );
                }
            }
        }
        return null;
    }

    // Metoda do pobierania roli użytkownika
    public String getUserRole(String email) throws SQLException {
        String sql = "SELECT role FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        }
        return null; // Zwraca null, jeśli użytkownik nie został znaleziony
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM \"user\" WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public Users getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM \"user\" WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Users(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("surname"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("role"),
                    rs.getBoolean("is_blocked")
                );
            }
        }
        return null;
    }

    public boolean toggleUserBlock(int userId) throws SQLException {
        String sql = "UPDATE \"user\" SET is_blocked = NOT is_blocked WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM \"user\" WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

}

