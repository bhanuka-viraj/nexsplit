package com.nexsplit.config.security;

import com.nexsplit.config.ApiConfig;
import com.nexsplit.service.impl.CustomUserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final CustomUserDetailsServiceImpl userDetailsService;
    private final ApiConfig apiConfig;

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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(apiConfig.getAllowedOrigins()));
        config.setAllowedMethods(List.of(apiConfig.getAllowedMethods()));
        config.setAllowedHeaders(List.of(apiConfig.getAllowedHeaders()));
        config.setAllowCredentials(apiConfig.isAllowedCredentials());
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}