package com.chrislomeli.mailermicroservice.controller;

import com.chrislomeli.mailermicroservice.service.SendGridMailer;
import com.chrislomeli.mailermicroservice.service.SendgridRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sendgrid.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
public class SendgridHandler {

    @Value("${sendgrid.batchsize.max:50}")
    int chunkSize;

    static final  ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
    }

    final SendGridMailer sendGridMailer;

    public SendgridHandler(SendGridMailer sendGridMailer) {
        this.sendGridMailer = sendGridMailer;
    }

    public String requestHandler(String jsonString) throws IOException {
        SendgridRequest mailRequest = objectMapper.readValue(jsonString, SendgridRequest.class);
        Response response = sendGridMailer.send(mailRequest);
        return objectMapper.writeValueAsString(response);
    }

}
