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

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService
    ) {
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
        Set<String> roles = user.getRoles().stream()
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
