package com.project.ssi_wypozyczalnia.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.ssi_wypozyczalnia.config.DatabaseConnection;
import com.project.ssi_wypozyczalnia.config.ObjectMapperConfig;
import com.project.ssi_wypozyczalnia.dao.ReservationDAO;
import com.project.ssi_wypozyczalnia.entity.Reservation;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@WebServlet("/api/reservation/*")
public class ReservationServlet extends HttpServlet {
    private final ObjectMapper objectMapper = ObjectMapperConfig.createObjectMapper();
    private ReservationDAO reservationDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.reservationDAO = new ReservationDAO(connection);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.startsWith("/bike/")) {
                int bikeId = Integer.parseInt(pathInfo.split("/")[2]);
                List<Reservation> reservations = reservationDAO.getReservationsByBikeId(bikeId);
                objectMapper.writeValue(response.getWriter(), reservations);
            } else if (pathInfo != null && pathInfo.startsWith("/user/")) {
                int userId = Integer.parseInt(pathInfo.split("/")[2]);
                List<Reservation> reservations = reservationDAO.getReservationByUserId(userId);
                objectMapper.writeValue(response.getWriter(), reservations);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\": \"Invalid request path\"}");
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Database error occurred\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Reservation reservation = objectMapper.readValue(request.getReader(), Reservation.class);
            reservationDAO.addReservation(reservation);
            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getWriter(), reservation);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Database error occurred\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || !pathInfo.matches("/\\d+")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Invalid reservation ID\"}");
            return;
        }

        try {
            int reservationId = Integer.parseInt(pathInfo.substring(1));

            Reservation reservation = reservationDAO.getReservationById(reservationId);
            if (reservation == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\": \"Reservation not found\"}");
                return;
            }

            long daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), reservation.getStartDate());
            if (daysUntilStart < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\": \"Reservation can only be cancelled at least 2 days before start date\"}");
                return;
            }

            reservationDAO.deleteReservation(reservationId);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Database error occurred\"}");
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Invalid reservation ID format\"}");
        }
    }
}
