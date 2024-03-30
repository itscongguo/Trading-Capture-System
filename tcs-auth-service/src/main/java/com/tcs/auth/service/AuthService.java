package com.tcs.auth.service;

import com.tcs.auth.dto.*;
import com.tcs.auth.security.JwtTokenProvider;
import com.tcs.common.exception.ErrorCode;
import com.tcs.common.exception.TcsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Authentication service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.userId}")
    private String adminUserId;

    @Value("${app.admin.accountId}")
    private String adminAccountId;

    /**
     * User login
     */
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // Validate credentials (hardcoded admin user)
        if (!adminUsername.equals(request.getUsername()) ||
            !adminPassword.equals(request.getPassword())) {
            log.warn("Invalid login attempt for user: {}", request.getUsername());
            throw new TcsException(ErrorCode.UNAUTHORIZED, "Invalid username or password");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                adminUserId, adminUsername, adminAccountId, "ADMIN");
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                adminUserId, adminUsername);

        // Store refresh token in Redis
        String refreshTokenKey = "refresh_token:" + adminUsername;
        redisTemplate.opsForValue().set(
                refreshTokenKey,
                refreshToken,
                7,
                TimeUnit.DAYS
        );

        log.info("User {} logged in successfully", request.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationInSeconds())
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(adminUserId)
                        .username(adminUsername)
                        .accountId(adminAccountId)
                        .role("ADMIN")
                        .build())
                .build();
    }

    /**
     * Refresh access token
     */
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new TcsException(ErrorCode.UNAUTHORIZED, "Invalid refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenTypeFromToken(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new TcsException(ErrorCode.UNAUTHORIZED, "Token is not a refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // Verify refresh token exists in Redis
        String refreshTokenKey = "refresh_token:" + username;
        String storedToken = redisTemplate.opsForValue().get(refreshTokenKey);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new TcsException(ErrorCode.UNAUTHORIZED, "Refresh token not found or expired");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                userId, username, adminAccountId, "ADMIN");

        log.info("Access token refreshed for user: {}", username);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationInSeconds())
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(userId)
                        .username(username)
                        .accountId(adminAccountId)
                        .role("ADMIN")
                        .build())
                .build();
    }

    /**
     * Validate token
     */
    public ValidateTokenResponse validateToken(ValidateTokenRequest request) {
        String token = request.getToken();

        if (!jwtTokenProvider.validateToken(token)) {
            return ValidateTokenResponse.builder()
                    .valid(false)
                    .build();
        }

        try {
            String tokenType = jwtTokenProvider.getTokenTypeFromToken(token);
            if (!"access".equals(tokenType)) {
                return ValidateTokenResponse.builder()
                        .valid(false)
                        .build();
            }

            String userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String accountId = jwtTokenProvider.getAccountIdFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            return ValidateTokenResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .username(username)
                    .accountId(accountId)
                    .role(role)
                    .build();
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return ValidateTokenResponse.builder()
                    .valid(false)
                    .build();
        }
    }

    /**
     * Logout
     */
    public void logout(String username) {
        // Remove refresh token from Redis
        String refreshTokenKey = "refresh_token:" + username;
        redisTemplate.delete(refreshTokenKey);
        log.info("User {} logged out successfully", username);
    }
}
