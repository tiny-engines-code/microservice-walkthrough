package com.chrislomeli.sendgridmailer.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("api.connection")
@Data
public class SendgridProperties {
    @Value("${sendgrid.auth.key}")
    String apiKeyValue;

    @Value("${sendgrid.api.version:4.7.0}")
    String sdkVersion;

    @Value("${sendgrid.version:v3}")
    String apiVersion;

    @Value("${sendgrid.host:api.sendgrid.com}")
    String host;

}
