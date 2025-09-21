package com.nexsplit.config.filter;

import com.nexsplit.config.ApiConfig;
import com.nexsplit.service.impl.CustomUserDetailsServiceImpl;
import com.nexsplit.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsServiceImpl userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith(ApiConfig.API_BASE_PATH + "/auth/") ||
                path.startsWith("/login/oauth2/code/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/actuator/") ||
                path.startsWith(ApiConfig.API_BASE_PATH + "/users/validate/") ||
                path.equals(ApiConfig.API_BASE_PATH + "/auth/login") ||
                path.equals(ApiConfig.API_BASE_PATH + "/auth/register") ||
                path.equals(ApiConfig.API_BASE_PATH + "/auth/oauth-login") ||
                path.equals(ApiConfig.API_BASE_PATH + "/users/validate-password") ||
                path.equals(ApiConfig.API_BASE_PATH + "/users/request-password-reset") ||
                path.equals(ApiConfig.API_BASE_PATH + "/users/reset-password");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid Authorization header found for path: {}", request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        log.debug("Processing JWT token: {}...", token.substring(0, Math.min(8, token.length())));

        if (jwtUtil.validateToken(token)) {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                String email = jwtUtil.getEmailFromToken(token);

                log.info("JWT Token parsed - UserId: {}, Email: {}, Role: {}, Token: {}...",
                        userId, email, role, token.substring(0, Math.min(20, token.length())));

                // Load UserDetails from the database
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("Authenticated user with ID: {} and role: {}", userId, role);
            } else {
                log.debug("User already authenticated, skipping token processing");
            }
        } else {
            log.warn("Invalid or expired JWT token");
            response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        }

        chain.doFilter(request, response);
    }

}
