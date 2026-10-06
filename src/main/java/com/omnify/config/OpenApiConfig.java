package com.omnify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.omnify.common.constant.OpenApiConstant;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Omnify API")
                .description("REST API documentation for the Omnify omnichannel system")
                .version("1.0.0")
                .contact(new Contact().name("Omnify Team")))
            .components(new Components()
                .addSecuritySchemes(OpenApiConstant.SECURITY_SCHEME_NAME, new SecurityScheme()
                    .name(OpenApiConstant.SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
