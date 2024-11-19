package com.project.ssi_wypozyczalnia.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.ssi_wypozyczalnia.config.DatabaseConnection;
import com.project.ssi_wypozyczalnia.dao.NoticeBoardDAO;
import com.project.ssi_wypozyczalnia.entity.NoticeBoard;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/noticeboard/*")
public class NoticeBoardServlet extends HttpServlet {
    private NoticeBoardDAO noticeBoardDAO;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        this.noticeBoardDAO = new NoticeBoardDAO(connection);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {   // Przeglądanie całej tablicy ogłoszeń
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Pobranie całej tablicy ogłoszeń
                List<NoticeBoard> board = noticeBoardDAO.getAllNoticeBoardsSorted();
                resp.setContentType("application/json");
                resp.getWriter().write(objectMapper.writeValueAsString(board));
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Błąd serwera: " + e.getMessage());
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            NoticeBoard noticeBoard = objectMapper.readValue(req.getInputStream(), NoticeBoard.class); // Odczyt JSON z żądania
            noticeBoardDAO.addNoticeBoard(noticeBoard); // Dodanie ogłoszenia przez DAO
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("Ogłoszenie dodane ");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Błąd serwera: " + e.getMessage());
        }
    }
}
