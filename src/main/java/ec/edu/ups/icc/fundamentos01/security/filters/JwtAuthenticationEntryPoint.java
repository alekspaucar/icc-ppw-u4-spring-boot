package ec.edu.ups.icc.fundamentos01.security.filters;

import java.io.IOException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        logger.error("Error de autenticación: {}", authException.getMessage());

        /*
         * Se configura el código HTTP 401 Unauthorized.
         */
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        /*
         * Se indica que la respuesta será JSON.
         */
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        /*
         * Se construye manualmente el JSON para evitar depender de ObjectMapper.
         */
        String jsonResponse = """
                {
                  "timestamp": "%s",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "Token de autenticación inválido o ausente",
                  "path": "%s",
                  "details": null
                }
                """.formatted(
                LocalDateTime.now(),
                request.getRequestURI());

        /*
         * Se escribe la respuesta directamente en el cuerpo HTTP.
         */
        response.getWriter().write(jsonResponse);
    }
}
