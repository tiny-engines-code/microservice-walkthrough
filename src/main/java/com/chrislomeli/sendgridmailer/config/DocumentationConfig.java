package com.chrislomeli.sendgridmailer.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentationConfig {
    @Bean
    @ConditionalOnProperty(prefix = "feature.toggle", name = "openapi", havingValue="true")
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title("Sendgrid Mailer API").description(
                        "Simple microservice tutorial"));
    }
}
