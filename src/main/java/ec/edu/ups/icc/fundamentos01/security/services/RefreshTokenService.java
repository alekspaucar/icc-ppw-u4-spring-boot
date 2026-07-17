package ec.edu.ups.icc.fundamentos01.security.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.security.config.JwtProperties;
import ec.edu.ups.icc.fundamentos01.security.entities.RefreshTokenEntity;
import ec.edu.ups.icc.fundamentos01.security.repositories.RefreshTokenRepository;
import ec.edu.ups.icc.fundamentos01.security.utils.JwtUtil;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtUtil jwtUtil,
            JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public RefreshTokenEntity createRefreshToken(UserEntity user, UserDetailsImpl userDetails) {
        String token = jwtUtil.generateRefreshToken(userDetails);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plus(Duration.ofMillis(jwtProperties.getRefreshExpiration()));

        RefreshTokenEntity refreshToken = new RefreshTokenEntity(user, token, expiresAt);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshTokenEntity validateAndGetActiveToken(String token) {
        if (!jwtUtil.validateRefreshToken(token)) {
            throw new BadRequestException("Refresh token inválido");
        }

        RefreshTokenEntity refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new BadRequestException("Refresh token no encontrado o revocado"));

        if (refreshToken.isExpired()) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new BadRequestException("Refresh token expirado");
        }

        if (refreshToken.getUser() == null || refreshToken.getUser().isDeleted()) {
            throw new BadRequestException("Usuario no válido para este refresh token");
        }

        return refreshToken;
    }

    @Transactional
    public void revoke(RefreshTokenEntity refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeAllByUser(UserEntity user) {
        List<RefreshTokenEntity> tokens = refreshTokenRepository
                .findByUserIdAndRevokedFalse(user.getId());
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }
}
