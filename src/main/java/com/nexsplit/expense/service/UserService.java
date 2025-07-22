package com.nexsplit.expense.service;

import com.nexsplit.expense.model.User;
import com.nexsplit.expense.repository.UserRepository;
import com.nexsplit.expense.util.JwtUtil;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public User processOAuthUser(OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName() != null ? oidcUser.getFullName() : "Unknown";

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(UUID.randomUUID().toString());
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setRole(User.Role.USER);
                    newUser.setCreatedAt(LocalDateTime.now());
                    return newUser;
                });

        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
        }

        return userRepository.save(user);
    }

    public String generateJwtForOAuthUser(OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        String role = userRepository.findByEmail(email)
                .map(User::getRole)
                .map(Enum::name)
                .orElse("USER");
        return jwtUtil.generateToken(email, role);
    }
}