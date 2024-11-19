package com.project.ssi_wypozyczalnia.dao;

import com.project.ssi_wypozyczalnia.entity.Reservation;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private final Connection connection;

    public ReservationDAO(Connection connection) {
        this.connection = connection;
    }

    public void addReservation(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservation (user_id, bike_id, start_date, end_date, total_price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservation.getUserId());
            stmt.setInt(2, reservation.getBikeId());
            stmt.setDate(3, Date.valueOf(reservation.getStartDate()));
            stmt.setDate(4, Date.valueOf(reservation.getEndDate()));
            stmt.setDouble(5, reservation.getTotalPrice());
            stmt.executeUpdate();
        }
    }

    public List<Reservation> getReservationByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE user_id = ?";
        List<Reservation> reservationList = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservationList.add(mapResultSetToReservation(rs));
                }
            }
        }
        return reservationList;
    }

    public List<LocalDate[]> getReservationDateByBike(int bikeId) throws SQLException {
        String sql = "SELECT start_date, end_date FROM reservation WHERE bike_id = ?";
        List<LocalDate[]> reservationDates = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bikeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate startDate = rs.getDate("start_date").toLocalDate();
                    LocalDate endDate = rs.getDate("end_date").toLocalDate();
                    reservationDates.add(new LocalDate[]{startDate, endDate});
                }
            }
        }
        return reservationDates;
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("bike_id"),
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date").toLocalDate(),
                rs.getDouble("total_price")
        );
    }
}
