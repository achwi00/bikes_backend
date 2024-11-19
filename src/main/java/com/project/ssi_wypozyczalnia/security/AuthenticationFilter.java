package com.project.ssi_wypozyczalnia.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(
        urlPatterns = "/api/*",
        filterName = "authenticationFilter"
)
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Allow login endpoint without authentication
        if (httpRequest.getRequestURI().endsWith("/api/users/login") ||
                httpRequest.getRequestURI().endsWith("/api/users/register")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = JwtUtil.validateToken(token);
            request.setAttribute("userEmail", claims.getSubject());
            request.setAttribute("userRole", claims.get("role"));
            chain.doFilter(request, response);
        } catch (JwtException e) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
} 