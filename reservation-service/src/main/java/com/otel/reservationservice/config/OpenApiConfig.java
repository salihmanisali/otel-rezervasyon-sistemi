package com.otel.reservationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${openapi.info.title:Reservation Service API}")
    private String title;

    @Value("${openapi.info.description:API for managing hotel reservations}")
    private String description;

    @Value("${openapi.info.version:1.0.0}")
    private String version;

    @Value("${openapi.info.contact.name:Reservation Service Team}")
    private String contactName;

    @Value("${openapi.info.contact.email:reservation@example.com}")
    private String contactEmail;

    @Bean
    public OpenAPI reservationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .servers(List.of(
                        new Server().url("/").description("Default Server URL")
                ));
    }
}