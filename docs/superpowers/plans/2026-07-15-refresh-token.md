# Práctica 16 - Refresh Token con JWT Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a refresh-token flow to the existing JWT auth so access tokens (30 min) can be renewed via a longer-lived, DB-backed, rotating refresh token (7 days), without allowing a refresh token to be used as an access token.

**Architecture:** JWTs get a `type` claim (`access` | `refresh`). `JwtAuthenticationFilter` only accepts `access`. A new `RefreshTokenEntity`/`RefreshTokenRepository` persist refresh tokens so they can be looked up, expired, and revoked. `RefreshTokenService` owns create/validate/revoke/rotate logic. `AuthService` gets `refresh()` and `logout()` methods and updates `login()`/`register()` to also issue+store a refresh token. `AuthController` exposes `POST /auth/refresh` and `POST /auth/logout`.

**Tech Stack:** Spring Boot, Spring Security, `io.jsonwebtoken` (jjwt), Spring Data JPA, PostgreSQL, Gradle.

## Global Constraints

- Package root: `ec.edu.ups.icc.fundamentos01`.
- `jwt.expiration` (access, 30 min) and `jwt.refresh-expiration` (refresh, 7 days = 604800000 ms) are already configured in `src/main/resources/application.yml` — do not touch config files.
- `/auth/**` is already `permitAll()` in `SecurityConfig` — do not touch that file.
- Build/verify with Gradle: `./gradlew compileJava -q` after each task; full run with `./gradlew bootRun`.
- Do not keep dead code: the old `JwtUtil.generateToken(Authentication)` / `generateTokenFromUserDetails(UserDetailsImpl)` methods are only called from `AuthService`, which this plan rewrites to call `generateAccessToken`/`generateAccessTokenFromUserDetails` directly — remove the old methods instead of keeping them as unused back-compat shims.
- There is currently a stray, empty, wrong-package stub at `security/utils/RefreshTokenService.java` (untracked) — delete it; the real service belongs in `security/services/` per the existing package layout (`services/AuthService.java`, `services/UserDetailsServiceImpl.java`).
- `RefreshTokenEntity.java` currently exists but is an empty (0-byte) file — it needs real content, not a new file.
- No test framework is currently exercised for the auth flow (only the default Spring Boot context-load test exists). Verification for this plan is: it compiles, and a full manual HTTP walkthrough (task 11) matching the practice's own §23 test plan, run with `curl` against `./gradlew bootRun`.

---

### Task 1: RefreshTokenEntity

**Files:**
- Modify (currently empty): `src/main/java/ec/edu/ups/icc/fundamentos01/security/entities/RefreshTokenEntity.java`

**Interfaces:**
- Consumes: `BaseEntity` (`getId()`, `isDeleted()` via `deleted` inherited), `UserEntity`.
- Produces: `RefreshTokenEntity(UserEntity user, String token, LocalDateTime expiresAt)`, `getUser()`, `getToken()`, `getExpiresAt()`, `isRevoked()`/`setRevoked(boolean)`, `isExpired()` — used by `RefreshTokenRepository` (Task 2) and `RefreshTokenService` (Task 8).

- [ ] **Step 1: Write the entity**

```java
package ec.edu.ups.icc.fundamentos01.security.entities;

import ec.edu.ups.icc.fundamentos01.core.entities.BaseEntity;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, unique = true, length = 1000)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    public RefreshTokenEntity() {
    }

    public RefreshTokenEntity(UserEntity user, String token, LocalDateTime expiresAt) {
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew compileJava -q`
Expected: build succeeds (no output on success with `-q`).

- [ ] **Step 3: Commit**

```bash
git add src/main/java/ec/edu/ups/icc/fundamentos01/security/entities/RefreshTokenEntity.java
git commit -m "feat: add RefreshTokenEntity"
```

---

### Task 2: RefreshTokenRepository

**Files:**
- Create: `src/main/java/ec/edu/ups/icc/fundamentos01/security/repositories/RefreshTokenRepository.java`

**Interfaces:**
- Consumes: `RefreshTokenEntity` (Task 1).
- Produces: `findByTokenAndRevokedFalse(String)`, `findByUserIdAndRevokedFalse(Long)` — used by `RefreshTokenService` (Task 8).

- [ ] **Step 1: Write the repository**

```java
package ec.edu.ups.icc.fundamentos01.security.repositories;

import ec.edu.ups.icc.fundamentos01.security.entities.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenAndRevokedFalse(String token);

    List<RefreshTokenEntity> findByUserIdAndRevokedFalse(Long userId);
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew compileJava -q`
Expected: build succeeds.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/ec/edu/ups/icc/fundamentos01/security/repositories/RefreshTokenRepository.java
git commit -m "feat: add RefreshTokenRepository"
```

---

### Task 3: RefreshTokenRequestDto

**Files:**
- Create: `src/main/java/ec/edu/ups/icc/fundamentos01/security/dtos/RefreshTokenRequestDto.java`

**Interfaces:**
- Produces: `RefreshTokenRequestDto(String refreshToken)`, `getRefreshToken()`/`setRefreshToken(String)` — used by `AuthController` (Task 10) as `@RequestBody` for `POST /auth/refresh` and `POST /auth/logout`.

- [ ] **Step 1: Write the DTO**

```java
package ec.edu.ups.icc.fundamentos01.security.dtos;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequestDto {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;

    public RefreshTokenRequestDto() {
    }

    public RefreshTokenRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew compileJava -q`
Expected: build succeeds.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/ec/edu/ups/icc/fundamentos01/security/dtos/RefreshTokenRequestDto.java
git commit -m "feat: add RefreshTokenRequestDto"
```

---

### Task 4: AuthResponseDto gets a refreshToken field

**Files:**
- Modify: `src/main/java/ec/edu/ups/icc/fundamentos01/security/dtos/AuthResponseDto.java`

**Interfaces:**
- Produces: `AuthResponseDto(String token, String refreshToken, Long userId, String name, String email, Set<String> roles)`, `getRefreshToken()`/`setRefreshToken(String)` — used by `AuthService.buildAuthResponse(...)` (Task 9).

- [ ] **Step 1: Add the field, update the constructor, add getter/setter**

Replace the whole file:

```java
package ec.edu.ups.icc.fundamentos01.security.dtos;

import java.util.Set;


public class AuthResponseDto {

    private String token;

    private String refreshToken;

    private String type = "Bearer";

    private Long userId;

    private String name;

    private String email;

    private Set<String> roles;

    public AuthResponseDto() {
    }

    public AuthResponseDto(
            String token,
            String refreshToken,
            Long userId,
            String name,
            String email,
            Set<String> roles
    ) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

}
```

- [ ] **Step 2: Compile**

Run: `./gradlew compileJava -q`
Expected: FAILS — `AuthService` still calls the old 5-arg `AuthResponseDto(...)` constructor. This is expected; it gets fixed in Task 9. Confirm the failure is only in `AuthService.java` (two call sites), not elsewhere.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/ec/edu/ups/icc/fundamentos01/security/dtos/AuthResponseDto.java
git commit -m "feat: add refreshToken field to AuthResponseDto"
```

---

### Task 5: Clean up JwtUtil (remove duplication, drop dead compat methods)

**Files:**
- Modify: `src/main/java/ec/edu/ups/icc/fundamentos01/security/utils/JwtUtil.java`

**Interfaces:**
- Consumes: `JwtProperties` (`getExpiration()`, `getRefreshExpiration()`, `getIssuer()`, `getSecret()`), `UserDetailsImpl`.
- Produces: `generateAccessToken(Authentication)`, `generateAccessTokenFromUserDetails(UserDetailsImpl)`, `generateRefreshToken(UserDetailsImpl)`, `getEmailFromToken(String)`, `getUserIdFromToken(String)`, `getTokenType(String)`, `validateToken(String)`, `validateAccessToken(String)`, `validateRefreshToken(String)` — used by `JwtAuthenticationFilter` (Task 6), `RefreshTokenService` (Task 8), `AuthService` (Task 9).

This file already has the new access/refresh methods added ad hoc alongside the old `generateToken`/`generateTokenFromUserDetails`, which duplicate the token-building logic. Replace the whole file so `buildToken` is the single place tokens get built, `getClaims` is the single place tokens get parsed, and the two now-dead legacy methods (`generateToken(Authentication)`, `generateTokenFromUserDetails(UserDetailsImpl)`) are removed since `AuthService` will call `generateAccessToken`/`generateAccessTokenFromUserDetails` directly (Task 9).

- [ ] **Step 1: Replace the file**

```java
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

    public String getEmailFromToken(String token) {
        return getClaims(token).get("email", String.class);
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public String getTokenType(String token) {
        return getClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

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
            logger.error("JWT claims string está vacío: {}", ex.getMessage());
        }

        return false;
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token) && ACCESS_TOKEN_TYPE.equals(getTokenType(token));
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token) && REFRESH_TOKEN_TYPE.equals(getTokenType(token));
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew compileJava -q`
Expected: still FAILS on `AuthService.java` (it still calls the now-removed `generateToken`/`generateTokenFromUserDetails`). Confirm the only errors are in `AuthService.java`.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/ec/edu/ups/icc/fundamentos01/security/utils/JwtUtil.java
git commit -m "refactor: dedupe JwtUtil token building/parsing, drop dead compat methods"
```

---

### Task 6: Clean up JwtAuthenticationFilter

**Files:**
- Modify: `src/main/java/ec/edu/ups/icc/fundamentos01/security/filters/JwtAuthenticationFilter.java`

**Interfaces:**
- Consumes: `JwtUtil.validateAccessToken(String)`, `JwtUtil.getEmailFromToken(String)` (Task 5).

The `validateToken` → `validateAccessToken` swap is already done in this file. Only the leftover stray `getLogger()` static accessor (unused, added by mistake) and a trailing blank line need to be removed.

- [ ] **Step 1: Read current file and remove the stray accessor**

Remove this block (added accidentally, never called anywhere):

```java
    public static Logger getLogger() {
        return logger;
    }

```

And remove the trailing blank line just before the closing `}` of the class (currently there's a stray blank line after the last method).

- [ ] **Step 2: Verify the condition is correct**

Confirm the file contains:

```java
            if (StringUtils.hasText(jwt) && jwtUtil.validateAccessToken(jwt)) {
```

(already present — no change needed here, just leave it).

- [ ] **Step 3: Compile**

Run: `./gradlew compileJava -q`
Expected: still FAILS only on `AuthService.java` (Task 9 fixes it).

- [ ] **Step 4: Commit**

```bash
git add src/main/java/ec/edu/ups/icc/fundamentos01/security/filters/JwtAuthenticationFilter.java
git commit -m "chore: remove stray unused accessor from JwtAuthenticationFilter"
```

---

### Task 7: Reject soft-deleted users at login

**Files:**
- Modify: `src/main/java/ec/edu/ups/icc/fundamentos01/users/repositories/UserRepository.java`
- Modify: `src/main/java/ec/edu/ups/icc/fundamentos01/security/services/UserDetailsServiceImpl.java`

**Interfaces:**
- Produces: `UserRepository.findByEmailAndDeletedFalse(String)` — used by `UserDetailsServiceImpl.loadUserByUsername(String)`.

- [ ] **Step 1: Add the repository method**

In `UserRepository.java`, add one line inside the interface (after `findByEmail`):

```java
    Optional<UserEntity> findByEmailAndDeletedFalse(String email);
```

Full file after the change:

```java
package ec.edu.ups.icc.fundamentos01.users.repositories;

import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByEmailAndDeletedFalse(String email);
    boolean existsByEmail(String email);
    boolean existsByIdAndDeletedFalse(Long id);
    Optional<UserEntity> findByIdAndDeletedFalse(Long id);
}
```

- [ ] **Step 2: Use it in UserDetailsServiceImpl**

Replace the whole file:

```java
package ec.edu.ups.icc.fundamentos01.security.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        return UserDetailsImpl.build(user);
    }
}
```

- [ ] **Step 3: Compile**

Run: `./gradlew compileJava -q`
Expected: still FAILS only on `AuthService.java`.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/ec/edu/ups/icc/fundamentos01/users/repositories/UserRepository.java src/main/java/ec/edu/ups/icc/fundamentos01/security/services/UserDetailsServiceImpl.java
git commit -m "fix: exclude soft-deleted users from login lookup"
```

---

### Task 8: RefreshTokenService

**Files:**
- Create: `src/main/java/ec/edu/ups/icc/fundamentos01/security/services/RefreshTokenService.java`
- Delete: `src/main/java/ec/edu/ups/icc/fundamentos01/security/utils/RefreshTokenService.java` (empty, wrong-package stub — untracked, safe to remove)

**Interfaces:**
- Consumes: `RefreshTokenRepository` (Task 2), `JwtUtil.generateRefreshToken`/`validateRefreshToken` (Task 5), `JwtProperties.getRefreshExpiration()`, `RefreshTokenEntity` (Task 1), `UserEntity`, `UserDetailsImpl`, `BadRequestException`.
- Produces: `createRefreshToken(UserEntity, UserDetailsImpl)`, `validateAndGetActiveToken(String)`, `revoke(RefreshTokenEntity)`, `revokeAllByUser(UserEntity)` — used by `AuthService` (Task 9).

- [ ] **Step 1: Delete the misplaced stub**

```bash
rm src/main/java/ec/edu/ups/icc/fundamentos01/security/utils/RefreshTokenService.java
```

- [ ] **Step 2: Write the real service**

```java
package ec.edu.ups.icc.fundamentos01.security.services;

import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.security.config.JwtProperties;
import ec.edu.ups.icc.fundamentos01.security.entities.RefreshTokenEntity;
import ec.edu.ups.icc.fundamentos01.security.repositories.RefreshTokenRepository;
import ec.edu.ups.icc.fundamentos01.security.utils.JwtUtil;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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

        tokens.forEach(token -> token.setRevoked(true));

        refreshTokenRepository.saveAll(tokens);
    }
}
```

- [ ] **Step 3: Compile**

Run: `./gradlew compileJava -q`
Expected: still FAILS only on `AuthService.java`.

- [ ] **Step 4: Commit**

```bash
git add -A src/main/java/ec/edu/ups/icc/fundamentos01/security/services/RefreshTokenService.java src/main/java/ec/edu/ups/icc/fundamentos01/security/utils/RefreshTokenService.java
git commit -m "feat: add RefreshTokenService, remove misplaced stub"
```

---

### Task 9: Rewrite AuthService (login, register, refresh, logout)

**Files:**
- Modify: `src/main/java/ec/edu/ups/icc/fundamentos01/security/services/AuthService.java`

**Interfaces:**
- Consumes: `RefreshTokenService` (Task 8), `JwtUtil.generateAccessToken`/`generateAccessTokenFromUserDetails` (Task 5), `AuthResponseDto` 6-arg constructor (Task 4), `RefreshTokenRequestDto` (Task 3), `UserRepository.findByIdAndDeletedFalse` (existing), `BadRequestException`, `ConflictException`.
- Produces: `login(LoginRequestDto)`, `register(RegisterRequestDto)`, `refresh(RefreshTokenRequestDto)`, `logout(RefreshTokenRequestDto)` — `refresh`/`logout` used by `AuthController` (Task 10).

- [ ] **Step 1: Replace the whole file**

```java
package ec.edu.ups.icc.fundamentos01.security.services;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RefreshTokenRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.fundamentos01.security.entities.RefreshTokenEntity;
import ec.edu.ups.icc.fundamentos01.security.entities.RoleEntity;
import ec.edu.ups.icc.fundamentos01.security.enums.RoleName;
import ec.edu.ups.icc.fundamentos01.security.repositories.RoleRepository;
import ec.edu.ups.icc.fundamentos01.security.utils.JwtUtil;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtil.generateAccessToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        UserEntity user = findActiveUserById(userDetails.getId());

        refreshTokenService.revokeAllByUser(user);

        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user, userDetails);

        return buildAuthResponse(accessToken, refreshToken.getToken(), user);
    }

    @Transactional
    public AuthResponseDto register(RegisterRequestDto registerRequest) {

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("El email ya esta registrado");
        }

        UserEntity user = new UserEntity();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

        RoleEntity userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new BadRequestException("Rol por defecto no encontrado"));

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        UserEntity savedUser = userRepository.save(user);

        UserDetailsImpl userDetails = UserDetailsImpl.build(savedUser);
        String accessToken = jwtUtil.generateAccessTokenFromUserDetails(userDetails);

        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(savedUser, userDetails);

        return buildAuthResponse(accessToken, refreshToken.getToken(), savedUser);
    }

    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto request) {

        RefreshTokenEntity currentRefreshToken =
                refreshTokenService.validateAndGetActiveToken(request.getRefreshToken());

        UserEntity user = currentRefreshToken.getUser();

        refreshTokenService.revoke(currentRefreshToken);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        String newAccessToken = jwtUtil.generateAccessTokenFromUserDetails(userDetails);

        RefreshTokenEntity newRefreshToken = refreshTokenService.createRefreshToken(user, userDetails);

        return buildAuthResponse(newAccessToken, newRefreshToken.getToken(), user);
    }

    @Transactional
    public void logout(RefreshTokenRequestDto request) {

        RefreshTokenEntity refreshToken =
                refreshTokenService.validateAndGetActiveToken(request.getRefreshToken());

        refreshTokenService.revoke(refreshToken);
    }

    private UserEntity findActiveUserById(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BadRequestException("Usuario no válido"));
    }

    private AuthResponseDto buildAuthResponse(String accessToken, String refreshToken, UserEntity user) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                roles
        );
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew compileJava -q`
Expected: build succeeds — all prior failures are now resolved.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/ec/edu/ups/icc/fundamentos01/security/services/AuthService.java
git commit -m "feat: wire refresh token issuance/rotation into AuthService"
```

---

### Task 10: AuthController gets /refresh and /logout

**Files:**
- Modify: `src/main/java/ec/edu/ups/icc/fundamentos01/security/controllers/AuthController.java`

**Interfaces:**
- Consumes: `AuthService.refresh(RefreshTokenRequestDto)`, `AuthService.logout(RefreshTokenRequestDto)` (Task 9).

- [ ] **Step 1: Replace the whole file**

```java
package ec.edu.ups.icc.fundamentos01.security.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.fundamentos01.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RefreshTokenRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.fundamentos01.security.services.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        AuthResponseDto response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        AuthResponseDto response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        AuthResponseDto response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequestDto request) {
        authService.logout(request);
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew compileJava -q`
Expected: build succeeds.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/ec/edu/ups/icc/fundamentos01/security/controllers/AuthController.java
git commit -m "feat: add /auth/refresh and /auth/logout endpoints"
```

---

### Task 11: End-to-end manual verification (matches practice §23)

**Files:** none (verification only). Requires a running PostgreSQL matching `application-dev.yml` (`jdbc:postgresql://localhost:5432/devdb`, user `ups`/`ups123`).

- [ ] **Step 1: Start the app**

Run: `./gradlew bootRun` (leave running in one terminal; run the following `curl` commands from another terminal).
Expected: log shows `Started Fundamentos01Application` with no errors, listening on `:8080` under context path `/api`.

- [ ] **Step 2: Register a test user**

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Usuario A","email":"usera@ups.edu.ec","password":"Password123"}'
```
Expected: `201 Created`, JSON body with non-null `token`, `refreshToken`, `roles: ["ROLE_USER"]`.

- [ ] **Step 3: Login and capture tokens**

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"usera@ups.edu.ec","password":"Password123"}'
```
Expected: `200 OK`, body has `token` (access) and `refreshToken`. Save both as `$ACCESS` and `$REFRESH` for the next steps.

- [ ] **Step 4: Consume a protected endpoint with the access token**

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/products/page?page=0\&size=5 \
  -H "Authorization: Bearer $ACCESS"
```
Expected: `200`.

- [ ] **Step 5: Try the same endpoint with the refresh token — must be rejected**

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/products/page?page=0\&size=5 \
  -H "Authorization: Bearer $REFRESH"
```
Expected: `401` (proves `validateAccessToken` rejects a `type: refresh` JWT).

- [ ] **Step 6: Renew tokens**

```bash
curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH\"}"
```
Expected: `200 OK`, new `token` and `refreshToken` different from the originals. Save as `$ACCESS2`/`$REFRESH2`.

- [ ] **Step 7: Reuse the old (now-revoked) refresh token — must fail**

```bash
curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH\"}"
```
Expected: `400 Bad Request`, message `Refresh token no encontrado o revocado`.

- [ ] **Step 8: Use the new access token**

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/products/page?page=0\&size=5 \
  -H "Authorization: Bearer $ACCESS2"
```
Expected: `200`.

- [ ] **Step 9: Logout**

```bash
curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH2\"}"
```
Expected: `204`.

- [ ] **Step 10: Refresh after logout — must fail**

```bash
curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH2\"}"
```
Expected: `400 Bad Request`.

- [ ] **Step 11: Stop the app**

Ctrl+C in the `bootRun` terminal.

- [ ] **Step 12: Note for README evidence (manual, not automatable)**

The practice's §28 asks for screenshots from Bruno/Postman of: login response, successful refresh, logout (204), and refresh-after-logout (400) — plus written answers to the three conceptual questions in §28. Screenshots require a GUI HTTP client and are outside what this plan can produce; capture them yourself against the same flow just verified with `curl`, then add them (and the three answers) to the README by hand.

No commit for this task — it's a verification pass over already-committed code.

---

## Self-Review Notes

- **Spec coverage:** §9 entity → Task 1; §10 repository → Task 2; §11 DTO → Task 3; §12 AuthResponseDto → Task 4; §13 JwtUtil → Task 5; §14 filter → Task 6; §15 RefreshTokenService → Task 8; §16 UserDetailsServiceImpl → Task 7; §17 AuthService → Task 9; §19 AuthController → Task 10; §20 SecurityConfig already satisfied (no task needed, confirmed `/auth/**` is `permitAll()`); §21 table created automatically via `ddl-auto: update` when the app starts (Task 11 Step 1); §23 test flow → Task 11; §28 evidence → Task 11 Step 12 (manual, flagged as such).
- **Deviation from the literal doc:** the doc's JwtUtil keeps `generateToken`/`generateTokenFromUserDetails` as "back-compat" wrappers. This plan removes them because nothing calls them once `AuthService` is rewritten in the same plan — keeping unused wrapper methods would be dead code. Flagged in Global Constraints.
- **Type consistency:** `AuthResponseDto` constructor is `(token, refreshToken, userId, name, email, roles)` everywhere it's called (Task 9's `buildAuthResponse`). `RefreshTokenService` method names (`createRefreshToken`, `validateAndGetActiveToken`, `revoke`, `revokeAllByUser`) match between Task 8's definition and Task 9's usage.
