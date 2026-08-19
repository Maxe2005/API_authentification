package com.imt.API_authentification.config;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Pose l'identifiant de correlation X-Request-Id dans le MDC au début de chaque requête et le
 * retire en fin de requête (story 2.1). Enregistré automatiquement comme {@code @Component} :
 * Spring Boot détecte tout bean {@code Filter} et le branche sur la chaîne de servlet, même sans
 * Spring Security — ainsi chaque log d'API_authentification (contrôleurs, services) porte l'ID
 * reçu via le forwarding de {@code TokenVerificationClient}.
 */
@Component
public class XRequestIdLoggingFilter extends OncePerRequestFilter {

    private static final String X_REQUEST_ID = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = request.getHeader(X_REQUEST_ID);
        if (requestId != null) {
            MDC.put(X_REQUEST_ID, requestId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(X_REQUEST_ID);
        }
    }
}