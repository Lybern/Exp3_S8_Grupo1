package com.minimarket.security.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import com.minimarket.security.monitor.SuspiciousActivityService;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    private SuspiciousActivityService suspiciousActivityService;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        suspiciousActivityService.recordUnauthorizedAccess(request, request.getRequestURI());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"error\": \"Acceso Denegado\", \"mensaje\": \"No tiene los permisos suficientes para acceder a este recurso.\"}");
    }
}
