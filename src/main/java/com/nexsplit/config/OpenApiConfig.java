package com.nexsplit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration for Swagger UI
 * 
 * This configuration:
 * - Defines the Bearer token security scheme for JWT authentication
 * - Sets up API documentation metadata
 * - Configures servers for different environments
 * - Enables the "Authorize" button in Swagger UI
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures OpenAPI documentation with security scheme and API information
     * 
     * @return OpenAPI configuration bean
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API Information
                .info(apiInfo())
                // Servers configuration
                .servers(servers())
                // Security scheme definition
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerAuthSecurityScheme()))
                // Global security requirement (applies to all endpoints)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * Defines the Bearer token security scheme for JWT authentication
     * 
     * @return SecurityScheme for Bearer token
     */
    private SecurityScheme bearerAuthSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT token in the format: Bearer <token>")
                .name("Authorization");
    }

    /**
     * Configures API information and metadata
     * 
     * @return Info object with API details
     */
    private Info apiInfo() {
        return new Info()
                .title("NexSplit API")
                .description("""
                        # NexSplit Expense Tracker API

                        ## Overview
                        RESTful API for the NexSplit expense tracking application.

                        ## Authentication
                        This API uses JWT (JSON Web Token) authentication. To access protected endpoints:
                        1. First, authenticate using `/api/v1/auth/login` or `/api/v1/auth/register`
                        2. Copy the `accessToken` from the response
                        3. Click the "Authorize" button above and enter: `Bearer <your-access-token>`
                        4. Now you can access protected endpoints

                        ## OAuth2 Login
                        You can also authenticate using Google OAuth2:
                        1. Navigate to `/oauth2/authorization/google` to start OAuth2 flow
                        2. After successful authentication, you'll receive JWT tokens
                        3. Use the access token as described above

                        ## Endpoints
                        - **Auth**: Registration, login, OAuth2, password reset
                        - **Users**: Profile management, validation
                        - **Expenses**: Expense tracking (coming soon)
                        - **Groups**: Group management (coming soon)

                        ## Response Format
                        All responses follow a consistent format:
                        ```json
                        {
                          "accessToken": "jwt-token-here",
                          "tokenType": "Bearer",
                          "expiresIn": 3600,
                          "email": "user@example.com",
                          "fullName": "John Doe"
                        }
                        ```
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("NexSplit Team")
                        .email("support@nexsplit.com")
                        .url("https://nexsplit.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * Configures server URLs for different environments
     * 
     * @return List of server configurations
     */
    private List<Server> servers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server"),
                new Server()
                        .url("http://95.111.248.142:8080")
                        .description("Development Server"));
    }
}
