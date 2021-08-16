package com.chrislomeli.mailermicroservice.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("api.connection")
public class SendgridProperties {
    @Value("${sendgrid.auth.key:noauth}")
    String apiKeyValue;

    @Value("${sendgrid.api.version:4.7.0}")
    String sdkVersion;

    @Value("${sendgrid.version:v3}")
    String apiVersion;

    @Value("${sendgrid.host:api.sendgrid.com}")
    String host;

    public void setApiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
