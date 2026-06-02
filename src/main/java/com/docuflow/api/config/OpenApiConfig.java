package com.docuflow.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI configuration.
 *
 * Defines the "bearerAuth" security scheme so controllers can reference it
 * with @SecurityRequirement(name = "bearerAuth").
 *
 * Access Swagger UI at: http://localhost:8080/swagger-ui
 * Paste your access token using the Authorize button (top right).
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "DocuFlow API",
        version = "1.0",
        description = "PDF editing and conversion SaaS"
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {
}
