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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user details for: {}", LoggingUtil.maskEmail(email));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found during authentication: {}", LoggingUtil.maskEmail(email));
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // For OAuth2 users who don't have a password, use a placeholder
        String password = user.getPassword() != null ? user.getPassword() : "{noop}oauth2user";

        log.debug("User details loaded successfully for: {}", LoggingUtil.maskEmail(email));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                password,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}