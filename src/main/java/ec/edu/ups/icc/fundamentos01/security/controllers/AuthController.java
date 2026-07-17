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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag (
        name = "Autenticación", description = "Operaciones relacionadas con la autenticación de usuarios"
)
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Iniciar sesión",
    description = "Permite a un usuario iniciar sesión y obtener un token JWT.")
    @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        AuthResponseDto response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Registrar usuario",
    description = "Permite registrar un nuevo usuario en el sistema.")
    @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        AuthResponseDto response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @Operation(summary = "Refrescar token",
    description = "Permite refrescar el token JWT utilizando un token de actualización válido.")
    @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @ApiResponse(responseCode = "401", description = "Token de actualización inválido o expirado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        AuthResponseDto response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Cerrar sesión",
    description = "Permite a un usuario cerrar sesión y revocar su token de actualización.")
    @ApiResponse(responseCode = "204", description = "Cierre de sesión exitoso")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @ApiResponse(responseCode = "401", description = "Token de actualización inválido o expirado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequestDto request) {
        authService.logout(request);
    }
}
