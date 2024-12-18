package com.project.ssi_wypozyczalnia.servlet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.ssi_wypozyczalnia.config.DatabaseConnection;
import com.project.ssi_wypozyczalnia.dao.BikeDAO;
import com.project.ssi_wypozyczalnia.entity.Bike;
import com.project.ssi_wypozyczalnia.entity.BikeSize;
import com.project.ssi_wypozyczalnia.entity.BikeType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/bikes/*")
public class BikeServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private BikeDAO bikeDAO;

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
        this.bikeDAO = new BikeDAO(connection);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Pobranie listy wszystkich rowerów
                List<Bike> bikes = bikeDAO.getAllBikes();
                resp.setContentType("application/json");
                resp.getWriter().write(objectMapper.writeValueAsString(bikes));
            } else if (pathInfo.startsWith("/search")) {
                // Wyszukiwanie rowerów po kryteriach
                handleSearchRequest(req, resp);
            } else {
                // Pobranie konkretnego roweru po ID
                String[] splits = pathInfo.split("/");
                if (splits.length == 2) {
                    int bikeId = Integer.parseInt(splits[1]);
                    Optional<Bike> bike = Optional.ofNullable(bikeDAO.getBikeById(bikeId));
                    if (bike.isPresent()) {
                        resp.setContentType("application/json");
                        resp.getWriter().write(objectMapper.writeValueAsString(bike.get()));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }
                }
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Błąd serwera: " + e.getMessage());
        }
    }

    private void handleSearchRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String sizeParam = req.getParameter("bike_size");
        String typeParam = req.getParameter("bike_type");
        String maxPriceParam = req.getParameter("maxPrice");

        if (sizeParam == null || typeParam == null || maxPriceParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Brak wymaganych parametrów: size, type, maxPrice");
            return;
        }

        try {
            BikeSize size = BikeSize.valueOf(sizeParam.toUpperCase());
            BikeType type = BikeType.valueOf(typeParam.toUpperCase());
            double maxPrice = Double.parseDouble(maxPriceParam);

            List<Bike> bikes = bikeDAO.searchBikes(size, type, maxPrice);
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(bikes));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Nieprawidłowy parametr: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.matches("/\\d+/toggle-availability")) {
            handleToggleAvailability(req, resp);
            return;
        }

        handleAddBike(req, resp);
    }

    private void handleToggleAvailability(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int bikeId = Integer.parseInt(req.getPathInfo().split("/")[1]);
            boolean updated = bikeDAO.toggleAvailability(bikeId);

            if (updated) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("Status dostępności został zmieniony");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Rower o podanym ID nie istnieje");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Nieprawidłowy format ID");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Błąd podczas zmiany dostępności: " + e.getMessage());
        }
    }

    private void handleAddBike(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Bike bike = objectMapper.readValue(req.getInputStream(), Bike.class);
            bikeDAO.addBike(bike);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("Rower został dodany pomyślnie");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Błąd podczas dodawania roweru: " + e.getMessage());
        } catch (JsonParseException | JsonMappingException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Nieprawidłowy format danych: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.split("/").length != 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Wymagany jest identyfikator roweru w ścieżce URL.");
            return;
        }

        try {
            int bikeId = Integer.parseInt(pathInfo.split("/")[1]);
            Bike bike = objectMapper.readValue(req.getInputStream(), Bike.class);
            bike.setId(bikeId);

            boolean updated = bikeDAO.updateBike(bike);
            if (updated) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("Rower zaktualizowany pomyślnie.");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Rower o ID " + bikeId + " nie został znaleziony.");
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Błąd serwera: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.split("/").length != 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Nieprawidłowe ID roweru");
            return;
        }

        try {
            int bikeId = Integer.parseInt(pathInfo.split("/")[1]);
            boolean deleted = bikeDAO.deleteBike(bikeId);

            if (deleted) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("Rower został usunięty");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Rower o podanym ID nie istnieje");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Nieprawidłowy format ID");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Błąd podczas usuwania roweru: " + e.getMessage());
        }
    }
}
