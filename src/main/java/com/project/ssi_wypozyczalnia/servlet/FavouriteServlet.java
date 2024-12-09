package com.project.ssi_wypozyczalnia.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.ssi_wypozyczalnia.config.DatabaseConnection;
import com.project.ssi_wypozyczalnia.dao.FavouriteDAO;
import com.project.ssi_wypozyczalnia.entity.Favourite;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/favourites/*")
public class FavouriteServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private FavouriteDAO favouriteDAO;

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
        this.favouriteDAO = new FavouriteDAO(connection);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userIdParam = req.getParameter("userId");

        if (userIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Brak wymaganego parametru: userId");
            return;
        }

        try {
            int userId = Integer.parseInt(userIdParam);
            List<Favourite> favourites = favouriteDAO.getAllFavouritesForUser(userId);
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(favourites));
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Błąd serwera: " + e.getMessage());
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Nieprawidłowy format parametru userId");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Favourite favourite;
        try {
            favourite = objectMapper.readValue(req.getInputStream(), Favourite.class);
            if (favourite.getUserId() == 0 || favourite.getBikeId() == 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Parametry userId i bikeId są wymagane");
                return;
            }

            favouriteDAO.addFavourite(favourite.getUserId(), favourite.getBikeId());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("Rower został dodany do ulubionych.");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Błąd serwera: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userIdParam = req.getParameter("userId");
        String bikeIdParam = req.getParameter("bikeId");

        if (userIdParam == null || bikeIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Parametry userId i bikeId są wymagane");
            return;
        }

        try {
            int userId = Integer.parseInt(userIdParam);
            int bikeId = Integer.parseInt(bikeIdParam);

            boolean removed = favouriteDAO.removeFavourite(userId, bikeId);
            if (removed) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("Rower został usunięty z ulubionych.");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Nie znaleziono takiego ulubionego roweru dla danego użytkownika.");
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Błąd serwera: " + e.getMessage());
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Nieprawidłowy format parametrów userId lub bikeId");
        }
    }
}
