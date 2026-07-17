package ec.edu.ups.icc.fundamentos01.security.utils;

import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import ec.edu.ups.icc.fundamentos01.security.config.JwtProperties;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    // ─── Generación ─────────────────────────────────────────────────────────

    public String generateAccessToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return buildToken(userPrincipal, jwtProperties.getExpiration(), ACCESS_TOKEN_TYPE);
    }

    public String generateAccessTokenFromUserDetails(UserDetailsImpl userDetails) {
        return buildToken(userDetails, jwtProperties.getExpiration(), ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(UserDetailsImpl userDetails) {
        return buildToken(userDetails, jwtProperties.getRefreshExpiration(), REFRESH_TOKEN_TYPE);
    }

    // Alias para compatibilidad con llamadas anteriores
    public String generateToken(Authentication authentication) {
        return generateAccessToken(authentication);
    }

    public String generateTokenFromUserDetails(UserDetailsImpl userDetails) {
        return generateAccessTokenFromUserDetails(userDetails);
    }

    // ─── Extracción de claims ────────────────────────────────────────────────

    public String getEmailFromToken(String token) {
        return getClaims(token).get("email", String.class);
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public String getTokenType(String token) {
        return getClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    // ─── Validación ──────────────────────────────────────────────────────────

    /*
     * Valida firma, formato y expiración.
     * No verifica el claim 'type'.
     */
    public boolean validateToken(String authToken) {
        try {
            getClaims(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Firma JWT inválida: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Token JWT malformado: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Token JWT expirado: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Token JWT no soportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string vacío: {}", ex.getMessage());
        }
        return false;
    }

    /*
     * Valida que sea un access token válido.
     *
     * Parsea el JWT UNA SOLA VEZ para evitar doble parseo y excepciones en
     * getTokenType(). Acepta tokens sin claim 'type' (retrocompatibilidad con
     * tokens generados antes de Práctica 16).
     */
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = getClaims(token);
            String type = claims.get(TOKEN_TYPE_CLAIM, String.class);
            // type == null → token anterior sin claim (retrocompatible) → aceptar
            // type == "access" → token nuevo → aceptar
            // type == "refresh" → token de refresh → rechazar
            return type == null || ACCESS_TOKEN_TYPE.equals(type);
        } catch (Exception ex) {
            logger.error("validateAccessToken falló: {} — {}", ex.getClass().getSimpleName(), ex.getMessage());
            return false;
        }
    }

    /*
     * Valida que sea un refresh token válido.
     *
     * Requiere claim 'type' = 'refresh'. Tokens sin claim son rechazados.
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            String type = claims.get(TOKEN_TYPE_CLAIM, String.class);
            return REFRESH_TOKEN_TYPE.equals(type);
        } catch (Exception ex) {
            logger.error("validateRefreshToken falló: {} — {}", ex.getClass().getSimpleName(), ex.getMessage());
            return false;
        }
    }

    // ─── Helpers privados ────────────────────────────────────────────────────

    private String buildToken(UserDetailsImpl userDetails, Long expirationMs, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(String.valueOf(userDetails.getId()))
                .claim("email", userDetails.getEmail())
                .claim("name", userDetails.getName())
                .claim("roles", roles)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
