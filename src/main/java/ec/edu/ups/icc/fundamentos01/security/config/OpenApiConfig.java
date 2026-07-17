package ec.edu.ups.icc.fundamentos01.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info()
                .title("API de programacion y plataformas web")
                .version("1.0.0")
                .description("API REST para la gestión de usuarios y roles en una aplicación Spring Boot.");
        
        Server localServer = new Server()
                .url("/api")
                .description("Servidor local");

        /*
         * Esquema de seguridad Bearer JWT.
         * Esto habilita el botón Authorize en Swagger UI.
         */
        SecurityScheme bearerScheme = new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Ingrese el JWT generado en /auth/login");

        Components components = new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme); 
        return new OpenAPI()
                .info(info)
                .addServersItem(localServer)
                .components(components)
                .addSecurityItem(
                        new SecurityRequirement().addList(SECURITY_SCHEME_NAME)
                );
        }
}