package com.nexsplit.service;

import com.nexsplit.dto.auth.RefreshTokenResponse;
import com.nexsplit.service.impl.RefreshTokenServiceImpl;

public interface RefreshTokenService {
    String generateRefreshToken(String userId, String ipAddress, String userAgent);
    RefreshTokenResponse refreshAccessToken(String refreshTokenValue, String ipAddress, String userAgent);
    void revokeAllUserTokens(String userId);
    void scheduledCleanupExpiredTokens();
    void cleanupExpiredTokens();

}
