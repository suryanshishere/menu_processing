package com.shelfpulse.activation_automation.security;

import com.shelfpulse.activation_automation.config.ApplicationProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AiBackendAuthenticationFilter extends OncePerRequestFilter {

    private static final String AI_BACKEND_API_KEY_HEADER = "x-internal-api-key";
    private static final String PROCESSED_MENU_PATH = "/menu/processed_menu";

    private final ApplicationProperties applicationProperties;

    public AiBackendAuthenticationFilter(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        if (requestPath.equals(PROCESSED_MENU_PATH) && "POST".equalsIgnoreCase(request.getMethod())) {
            String providedKey = request.getHeader(AI_BACKEND_API_KEY_HEADER);
            String expectedKey = applicationProperties.getAiBackendSecretKey();

            if (providedKey == null || !providedKey.equals(expectedKey)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter()
                        .write("{\"error\":\"Forbidden: You do not have permission to access this resource.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        return !requestPath.equals(PROCESSED_MENU_PATH);
    }
}
