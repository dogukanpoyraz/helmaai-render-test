package com.backend.helmaaibackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String JWT_BEARER_SCHEME = "BearerAuth";

    @Bean
    public OpenAPI helmaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Helma AI Backend API")
                        .description("Yaşlı birey destek uygulaması için REST API (Auth, kullanıcı vb.)")
                        .version("v1")
                        .contact(new Contact().name("Helma Team").email("team@helma.ai"))
                        .license(new License().name("Apache-2.0")))
                .components(new Components()
                        .addSecuritySchemes(JWT_BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(JWT_BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                // Güvenli endpoint’ler için global security (auth dışındakilerde işe yarar)
                .addSecurityItem(new SecurityRequirement().addList(JWT_BEARER_SCHEME));
    }

    @Bean
    public GroupedOpenApi publicAuthApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .packagesToScan("com.backend.helmaaibackend.controller")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .packagesToScan("com.backend.helmaaibackend.controller")
                .build();
    }
}
