package ec.edu.ups.icc.fundamentos01.core.exceptions.handler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.naming.AuthenticationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ec.edu.ups.icc.fundamentos01.core.exceptions.base.AplicationException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(AplicationException.class)
        public ResponseEntity<ErrorResponse> handleApplicationException(
                        AplicationException exception,
                        HttpServletRequest request) {
                HttpStatus status = exception.getStatus();

                ErrorResponse response = new ErrorResponse(
                                LocalDateTime.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(status).body(response);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {
                Map<String, String> details = new HashMap<>();

                for (FieldError error : exception.getBindingResult().getFieldErrors()) {
                        details.put(error.getField(), error.getDefaultMessage());
                }

                ErrorResponse response = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                "Datos de entrada inválidos",
                                request.getRequestURI(),
                                details);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneralException(
                        Exception exception,
                        HttpServletRequest request) {
                ErrorResponse response = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                "Error interno del servidor",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        @ExceptionHandler(BindException.class)
        public ResponseEntity<ErrorResponse> handleBindException(
                        BindException exception,
                        HttpServletRequest request) {
                Map<String, String> details = new HashMap<>();

                for (FieldError error : exception.getBindingResult().getFieldErrors()) {
                        details.put(error.getField(), error.getDefaultMessage());
                }

                ErrorResponse response = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                "Parámetros de consulta inválidos",
                                request.getRequestURI(),
                                details);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /*
         * Maneja AuthorizationDeniedException.
         *
         * Esta excepción aparece cuando @PreAuthorize evalúa a false.
         *
         * Ejemplo:
         * Un usuario con ROLE_USER intenta acceder a:
         *
         * GET /api/products
         *
         * pero el endpoint requiere:
         *
         * @PreAuthorize("hasRole('ADMIN')")
         *
         * Resultado esperado:
         * 403 Forbidden
         */
        @ExceptionHandler(AuthorizationDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
                        AuthorizationDeniedException exception,
                        HttpServletRequest request) {
                ErrorResponse response = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                HttpStatus.FORBIDDEN.getReasonPhrase(),
                                "No tienes permisos para acceder a este recurso",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        /*
         * Maneja errores de acceso denegado.
         *
         * Se usa cuando:
         * - Un usuario intenta modificar un producto ajeno.
         * - Un usuario autenticado no tiene autorización contextual.
         *
         * El mensaje específico enviado desde el servicio
         * se conserva en la respuesta.
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(
                        AccessDeniedException exception,
                        HttpServletRequest request) {
                String message = exception.getMessage();

                if (message == null || message.isBlank()) {
                        message = "Acceso denegado";
                }

                ErrorResponse response = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                HttpStatus.FORBIDDEN.getReasonPhrase(),
                                message,
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(response);
        }

        /*
         * Maneja AuthenticationException.
         *
         * Esta excepción aparece cuando hay problemas de autenticación:
         * - credenciales incorrectas
         * - usuario no autenticado
         * - token inválido
         *
         * En la mayoría de casos JWT ya responde desde JwtAuthenticationEntryPoint,
         * pero este manejador sirve como respaldo.
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthenticationException(
                        AuthenticationException exception,
                        HttpServletRequest request) {
                ErrorResponse response = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                "Credenciales inválidas o sesión expirada",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
}