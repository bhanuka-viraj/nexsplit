package com.nexsplit.expense.service;

import com.nexsplit.expense.exception.UserUnauthorizedException;
import com.nexsplit.expense.model.User;
import com.nexsplit.expense.repository.UserRepository;
import com.nexsplit.expense.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public User processOAuthUser(OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName() != null ? oidcUser.getFullName() : "Unknown";

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .id(UUID.randomUUID().toString())
                        .email(email)
                        .name(name)
                        .role(User.Role.USER)
                        .build()));

        if (!name.equals(user.getName())) {
            user.setName(name);
            user = userRepository.save(user);
        }

        return user;
    }

    @Transactional
    public User registerUser(String email, String password, String name, Integer contactNumber) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (password.isEmpty()){
            throw new IllegalArgumentException("Password cannot be null");
        }
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .contactNumber(contactNumber)
                .role(User.Role.USER)
                .build();
        return userRepository.save(user);
    }

    public String loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return jwtUtil.generateAccessToken(email, user.getRole().name());
    }

    public String generateAccessToken(User user) {
        return jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
    }

    public String generateRefreshToken(String userEmail) {
        return jwtUtil.generateRefreshToken(userEmail);
    }

    public String generateAccessToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new UserUnauthorizedException("Invalid refresh token");
        }
        String email = jwtUtil.getEmailFromToken(refreshToken);
        User user = userRepository.getUserByEmail(email);
        return jwtUtil.generateAccessToken(email,user.getRole().name());
    }
}