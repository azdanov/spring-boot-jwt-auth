package org.js.azdanov.springbootjwtauth.security.services;

import org.js.azdanov.springbootjwtauth.models.RefreshToken;
import org.js.azdanov.springbootjwtauth.properties.JWTProperties;
import org.js.azdanov.springbootjwtauth.repository.RefreshTokenRepository;
import org.js.azdanov.springbootjwtauth.repository.UserRepository;
import org.js.azdanov.springbootjwtauth.security.exception.TokenRefreshException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final Duration refreshExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, JWTProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.refreshExpiration = jwtProperties.getRefreshExpiration();
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).orElseThrow());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration.toMillis()));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).orElseThrow());
    }
}
