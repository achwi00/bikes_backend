package com.project.ssi_wypozyczalnia.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.ssi_wypozyczalnia.dao.UserDAO;
import com.project.ssi_wypozyczalnia.entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.project.ssi_wypozyczalnia.config.DatabaseConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet
{
    private UserDAO userDAO;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException{
        super.init();
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.userDAO = new UserDAO(connection);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.split("/").length != 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Wymagany jest identyfikator usera w ścieżce URL.");
            return;
        }

        try {
            int userId = Integer.parseInt(pathInfo.split("/")[1]);
            Users users = objectMapper.readValue(req.getInputStream(), Users.class);
            users.setId(userId);

            boolean updated = userDAO.updateUser(users);
            if (updated) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("Pomyślnie zaktualizowano dane usera.");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("User o id " + userId + " nie został znaleziony.");
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Błąd serwera: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String pathInfo = req.getPathInfo();
        System.out.println("Path Info: " + pathInfo);

        if (pathInfo == null || pathInfo.split("/").length != 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Wymagany jest identyfikator użytkownika w ścieżce URL.");
            return;
        }

        try {
            int userId = Integer.parseInt(pathInfo.split("/")[1]);
            System.out.println("user o id: " + userId);
            // Pobierz użytkownika po ID z DAO
            Users user = userDAO.getUserById(userId);

            if (user != null) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(objectMapper.writeValueAsString(user));
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("User o id " + userId + " nie został znaleziony.");
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Błąd serwera: " + e.getMessage());
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Identyfikator użytkownika musi być liczbą.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        // Obsługa logowania użytkownika
        if ("/login".equals(pathInfo)) {
            try {
                // Odczytaj dane logowania z żądania
                Users loginRequest = objectMapper.readValue(req.getInputStream(), Users.class);
                String email = loginRequest.getEmail();
                String password = loginRequest.getPasswordHash();

                // Sprawdź poprawność danych logowania
                Users authenticatedUser = userDAO.authenticateUser(email, password);
                if (authenticatedUser != null) {
                    resp.setContentType("application/json");
                    resp.setCharacterEncoding("UTF-8");
                    resp.getWriter().write(objectMapper.writeValueAsString(authenticatedUser));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("Nieprawidłowy email lub hasło.");
                }
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("Błąd serwera: " + e.getMessage());
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Nieobsługiwany endpoint.");
        }
    }
}
