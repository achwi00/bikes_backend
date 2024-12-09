package com.project.ssi_wypozyczalnia.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.ssi_wypozyczalnia.config.DatabaseConnection;
import com.project.ssi_wypozyczalnia.dao.UserDAO;
import com.project.ssi_wypozyczalnia.entity.Users;
import com.project.ssi_wypozyczalnia.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
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
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        System.out.println("Path Info: " + pathInfo);

        if ("/me".equals(pathInfo)) {
            // Pobierz token z nagłówka Authorization
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("Brak tokenu autoryzacyjnego.");
                return;
            }

            String token = authHeader.substring(7); // Usuń "Bearer " z początku

            try {
                // Walidacja tokenu i pobranie danych użytkownika
                Claims claims = JwtUtil.validateToken(token);
                String email = claims.getSubject();

                // Pobierz użytkownika z bazy danych
                Users user = userDAO.getUserByEmail(email);
                if (user != null) {
                    resp.setContentType("application/json");
                    resp.setCharacterEncoding("UTF-8");
                    resp.getWriter().write(objectMapper.writeValueAsString(user));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("Użytkownik nie został znaleziony.");
                }
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("Nieprawidłowy token.");
            }
        } else {
            // Obsługa innych ścieżek
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
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if ("/register".equals(pathInfo)) {
            try {
                Users newUser = objectMapper.readValue(req.getInputStream(), Users.class);

                // Walidacja danych
                if (newUser.getEmail() == null || newUser.getPasswordHash() == null ||
                        newUser.getUsername() == null || newUser.getSurname() == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("Wszystkie pola są wymagane");
                    return;
                }

                // Sprawdzenie czy email już istnieje
                if (userDAO.emailExists(newUser.getEmail())) {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    resp.getWriter().write("Użytkownik o podanym emailu już istnieje");
                    return;
                }

                // Hashowanie hasła
                String hashedPassword = UserDAO.hashPassword(newUser.getPasswordHash());
                newUser.setPasswordHash(hashedPassword);

                // Ustawienie domyślnej roli
                newUser.setRole("USER");

                // Dodanie użytkownika
                userDAO.addUser(newUser);

                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write("Użytkownik został zarejestrowany pomyślnie");

            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("Błąd serwera: " + e.getMessage());
            }
        } else if ("/login".equals(pathInfo)) {
            try {
                Users loginRequest = objectMapper.readValue(req.getInputStream(), Users.class);
                String email = loginRequest.getEmail();
                String password = loginRequest.getPasswordHash();

                Users authenticatedUser = userDAO.authenticateUser(email, password);
                if (authenticatedUser != null) {
                    if (authenticatedUser.getIsBlocked()) {
                        throw new RuntimeException("Konto zablokowane");
                    }

                    String token = JwtUtil.generateToken(authenticatedUser.getEmail(), authenticatedUser.getRole());

                    Map<String, Object> response = new HashMap<>();
                    response.put("token", token);
                    response.put("user", authenticatedUser);

                    resp.setContentType("application/json");
                    resp.setCharacterEncoding("UTF-8");
                    resp.getWriter().write(objectMapper.writeValueAsString(response));
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
