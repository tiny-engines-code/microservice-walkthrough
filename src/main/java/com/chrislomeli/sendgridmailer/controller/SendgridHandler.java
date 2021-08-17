package com.chrislomeli.sendgridmailer.controller;

import com.chrislomeli.sendgridmailer.service.SendgridMailer;
import com.chrislomeli.sendgridmailer.service.SendgridRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sendgrid.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.function.Predicate;


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

    /* simple for a textbook implementation */
    Predicate<String> validEmail = s -> EmailValidator.getInstance().isValid(s);
    Predicate<String> validRequired = s -> s != null && !s.isEmpty();

     final SendgridMailer sendGridMailer;

    public SendgridHandler(SendgridMailer sendGridMailer) {
        this.sendGridMailer = sendGridMailer;
    }

    public Response requestHandler(String jsonString) throws IOException {
        try {
            //---convert json ----
            SendgridRequest mailRequest = objectMapper.readValue(jsonString, SendgridRequest.class);

            //--- pass to Sendgrigmailr
            var response  =  sendGridMailer.send(mailRequest);

            // ---- handle bad response and return ---
            if (response == null)
                throw new NullPointerException("mailer returned unexpected null");
            return  response;
        } catch (JsonProcessingException |  IllegalArgumentException ex) {
            return new Response(HttpStatus.BAD_REQUEST.value(), ex.getMessage(),
                    Map.of("Content-type", "application/json", "X-Source", "json-format"));
        }  catch (Exception ex) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(),
                    Map.of("Content-type", "application/json", "X-Source", "application-error"));
        }
    }

}
