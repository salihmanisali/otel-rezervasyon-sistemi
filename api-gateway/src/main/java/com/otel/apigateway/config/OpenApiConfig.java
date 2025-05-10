package com.otel.apigateway.config;

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

    @Value("${openapi.info.title:API Gateway}")
    private String title;

    @Value("${openapi.info.description:API Gateway for Hotel Reservation System}")
    private String description;

    @Value("${openapi.info.version:1.0.0}")
    private String version;

    @Value("${openapi.info.contact.name:API Gateway Team}")
    private String contactName;

    @Value("${openapi.info.contact.email:gateway@example.com}")
    private String contactEmail;

    @Bean
    public OpenAPI apiGatewayOpenAPI() {
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