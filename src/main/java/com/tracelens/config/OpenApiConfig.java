package com.tracelens.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME =
            "bearerAuth";

    @Bean
    public OpenAPI traceLensOpenApi() {

        SecurityScheme bearerSecurityScheme =
                new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                                "Enter the JWT access token returned "
                                        + "by POST /api/auth/login. "
                                        + "Do not include the word Bearer."
                        );

        return new OpenAPI()
                .info(
                        new Info()
                                .title("TraceLens AI API")
                                .version("1.0.0")
                                .description(
                                        """
                                        TraceLens AI is an AI-powered digital
                                        evidence analysis and investigation API.

                                        The API supports secure authentication,
                                        investigation cases, evidence storage,
                                        SHA-256 integrity verification, text
                                        extraction, persistent AI analysis,
                                        entity and timeline intelligence,
                                        investigator notes, dashboard analytics
                                        and final case reports.

                                        AI-generated output is investigative
                                        assistance only and must be independently
                                        reviewed and verified.
                                        """
                                )
                                .contact(
                                        new Contact()
                                                .name("TraceLens AI")
                                )
                                .license(
                                        new License()
                                                .name("MIT License")
                                )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(
                                        SECURITY_SCHEME_NAME
                                )
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        bearerSecurityScheme
                                )
                )
                .externalDocs(
                        new ExternalDocumentation()
                                .description(
                                        "TraceLens AI project documentation"
                                )
                );
    }
}