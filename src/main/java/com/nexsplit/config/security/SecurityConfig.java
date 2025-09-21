package com.nexsplit.config.security;

import com.nexsplit.config.ApiConfig;
import com.nexsplit.config.filter.JwtFilter;
import com.nexsplit.service.impl.CustomUserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for NexSplit application.
 * 
 * This configuration class sets up Spring Security with JWT-based
 * authentication,
 * CORS support, and role-based authorization. It provides comprehensive
 * security
 * features including:
 * 
 * - JWT token-based authentication
 * - OAuth2 integration for Google Sign-In
 * - Role-based access control (RBAC)
 * - CORS configuration for cross-origin requests
 * - Password encryption with BCrypt
 * - Session management
 * - Exception handling for authentication failures
 * 
 * Security Features:
 * - CSRF protection disabled for API endpoints
 * - Stateless session management
 * - JWT filter for token validation
 * - Custom authentication entry point
 * - Configurable CORS settings
 * - Admin role protection for admin endpoints
 * - Public access for authentication and documentation endpoints
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final CustomUserDetailsServiceImpl userDetailsService;
    private final ApiConfig apiConfig;

    /**
     * Configures the security filter chain for the application.
     * 
     * This method sets up the complete security configuration including:
     * - CSRF protection (disabled for API)
     * - CORS configuration
     * - Session management (stateless)
     * - Authorization rules for different endpoints
     * - Exception handling for authentication failures
     * - JWT filter integration
     * 
     * @param http HttpSecurity configuration builder
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ApiConfig.API_BASE_PATH + "/auth/**", "/login/oauth2/code/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
                        .requestMatchers(ApiConfig.API_BASE_PATH + "/admin/**").hasRole("ADMIN")
                        .requestMatchers(ApiConfig.API_BASE_PATH + "/users/validate/**").permitAll()
                        .requestMatchers(ApiConfig.API_BASE_PATH + "/users/request-password-reset").permitAll()
                        .requestMatchers(ApiConfig.API_BASE_PATH + "/users/reset-password").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // OAuth2 is now handled via token exchange endpoint /api/v1/auth/oauth2/verify
                // This provides better support for mobile apps and web apps using Google
                // Sign-In SDK

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // OAuth2 is now handled via token exchange endpoint /api/v1/auth/oauth2/verify
    // This provides better support for mobile apps and web apps using Google
    // Sign-In SDK

    /**
     * Configures the authentication manager for the application.
     * 
     * @param authenticationConfiguration Spring Security authentication
     *                                    configuration
     * @return Configured AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configures the password encoder for the application.
     * 
     * Uses BCrypt with strength 12 for secure password hashing.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) for the application.
     * 
     * This method sets up CORS configuration to allow cross-origin requests
     * from configured origins, methods, and headers.
     * 
     * @return CorsConfigurationSource with CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(apiConfig.getAllowedOrigins());
        config.setAllowedMethods(apiConfig.getAllowedMethods());
        config.setAllowedHeaders(apiConfig.getAllowedHeaders());
        config.setAllowCredentials(apiConfig.isAllowedCredentials());
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}