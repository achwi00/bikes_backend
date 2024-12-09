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
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {
    private UserDAO userDAO;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            Connection connection = DatabaseConnection.getConnection();
            this.userDAO = new UserDAO(connection);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isAdmin(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = JwtUtil.validateToken(token);
                String role = claims.get("role", String.class);
                return "ADMIN".equals(role);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (!isAdmin(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(objectMapper.writeValueAsString(
                    Map.of("error", "Brak uprawnień administratora")
            ));
            return;
        }

        String pathInfo = req.getPathInfo();

        if ("/users".equals(pathInfo)) {
            try {
                List<Users> users = userDAO.getAllUsers();
                resp.getWriter().write(objectMapper.writeValueAsString(users));
                resp.setStatus(HttpServletResponse.SC_OK);
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(objectMapper.writeValueAsString(
                        Map.of("error", "Błąd podczas pobierania użytkowników: " + e.getMessage())
                ));
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(objectMapper.writeValueAsString(
                    Map.of("error", "Nieznany endpoint")
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo.startsWith("/users/")) {
            String[] parts = pathInfo.split("/");
            if (parts.length != 4) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            int userId = Integer.parseInt(parts[2]);
            String action = parts[3];

            try {
                boolean success = false;
                if ("toggle-block".equals(action)) {
                    success = userDAO.toggleUserBlock(userId);
                }

                if (success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo.startsWith("/users/")) {
            try {
                int userId = Integer.parseInt(pathInfo.split("/")[2]);
                boolean success = userDAO.deleteUser(userId);

                if (success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    }


}
