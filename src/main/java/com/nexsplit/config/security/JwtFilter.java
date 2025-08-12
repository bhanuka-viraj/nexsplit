package com.nexsplit.config.security;

import com.nexsplit.config.ApiConfig;
import com.nexsplit.service.CustomUserDetailsService;
import com.nexsplit.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith(ApiConfig.API_BASE_PATH + "/auth/") ||
                path.startsWith("/login/oauth2/code/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/actuator/") ||
                path.startsWith(ApiConfig.API_BASE_PATH + "/users/validate/") ||
                path.equals(ApiConfig.API_BASE_PATH + "/users/request-password-reset") ||
                path.equals(ApiConfig.API_BASE_PATH + "/users/reset-password") ||
                path.equals(ApiConfig.API_BASE_PATH + "/auth/refresh") ||
                path.equals(ApiConfig.API_BASE_PATH + "/auth/refresh-token");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No valid Authorization header found for path: {}", request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        logger.debug("Processing JWT token: {}", token.substring(0, Math.min(10, token.length())) + "...");

        if (jwtUtil.validateToken(token)) {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = jwtUtil.getEmailFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                // Load UserDetails from the database
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.info("Authenticated user: {} with role: {}", email, role);
            }
        } else {
            logger.warn("Invalid or expired JWT token");
            response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        }

        chain.doFilter(request, response);
    }
}