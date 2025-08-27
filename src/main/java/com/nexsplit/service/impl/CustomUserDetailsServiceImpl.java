package com.nexsplit.service.impl;

import com.nexsplit.model.User;
import com.nexsplit.repository.UserRepository;
import com.nexsplit.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.debug("Loading user details for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found during authentication: {}", userId);
                    return new UsernameNotFoundException("User not found with id: " + userId);
                });

        // For OAuth2 users who don't have a password, use a placeholder
        String password = user.getPassword() != null ? user.getPassword() : "{noop}oauth2user";

        log.debug("User details loaded successfully for userId: {}", userId);
        return new org.springframework.security.core.userdetails.User(
                user.getId(), // Use userId as username
                password,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}