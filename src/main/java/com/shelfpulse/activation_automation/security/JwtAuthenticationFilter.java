package com.shelfpulse.activation_automation.security;

import com.shelfpulse.activation_automation.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String adminId = null;

        logger.debug("Processing request: " + request.getRequestURI());
        logger.debug("Authorization header present: " + (authHeader != null));

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                adminId = jwtUtil.extractUsername(token);
                logger.debug("Extracted adminId from token: " + adminId);
            } catch (Exception e) {
                logger.error("Error parsing JWT: " + e.getMessage());
            }
        } else {
            logger.debug("No Bearer token found in Authorization header");
        }

        if (adminId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(adminId);
                logger.debug("Loaded user details for adminId: " + adminId);

                if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication successful for adminId: " + adminId);
                } else {
                    logger.debug("Token validation failed for adminId: " + adminId);
                }
            } catch (Exception e) {
                logger.error("Error loading user details: " + e.getMessage());
            }
        } else if (adminId == null) {
            logger.debug("adminId is null, skipping authentication");
        }
        filterChain.doFilter(request, response);
    }
}
