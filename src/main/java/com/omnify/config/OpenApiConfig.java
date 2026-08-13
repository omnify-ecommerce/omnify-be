package com.omnify.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI omnifyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Omnify OMS/ERP API")
                        .description("Omnichannel E-commerce Management System — Auth, RBAC, Order, Inventory, Sync Engine")
                        .version("v1")
                        .contact(new Contact().name("Omnify Backend Team")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Nhập access token nhận được từ /api/v1/auth/login (không cần gõ chữ 'Bearer ')")));
    }
}