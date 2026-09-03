package com.example.productapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productAppOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Dashboard API")
                        .description("REST API for CRUD operations on products")
                        .version("v1"));
    }
}
