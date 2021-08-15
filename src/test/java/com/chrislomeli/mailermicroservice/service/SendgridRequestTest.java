package com.chrislomeli.mailermicroservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SendgridRequestTest {

    SendgridRequest sendgridRequest;
    ObjectMapper mapper = new ObjectMapper();

    @Test
    void basic_json_conversion_works() {
        String json = "{\n" +
                "  \"senderName\": \"me\",\n" +
                "  \"fromAddress\": \"sender@gmail.com\",\n" +
                "  \"toAddress\": \"receiver@gmail.com\",\n" +
                "  \"subject\": \"This is my email\",\n" +
                "  \"content\": \"Click here to create a click event: http://www.google.com\",\n" +
                "  \"customArgs\": {\n" +
                "    \"mycounter\": 60000\n" +
                "  }\n" +
                "}";
        try {sendgridRequest = mapper.readValue(json, SendgridRequest.class );} catch (Exception ignore) {}
        assertThat(sendgridRequest).isNotNull();
        assertThat(sendgridRequest.getSenderName()).isEqualTo("me");
        assertThat(sendgridRequest.getContent()).isEqualTo("Click here to create a click event: http://www.google.com");
        assertThat(sendgridRequest.getSubject()).isEqualTo("This is my email");
        assertThat(sendgridRequest.getToAddress()).isEqualTo("receiver@gmail.com");
        assertThat(sendgridRequest.getFromAddress()).isEqualTo("sender@gmail.com");
        assertThat(sendgridRequest.getCustomArgs()).isNotNull();
        assertThat(sendgridRequest.getCustomArgs().get("mycounter")).isEqualTo("60000");
    }

    @Test
    void json_conversion_with_empty() {
        String json = "{\n" +
                "  \"senderName\": \"me\",\n" +
                "  \"fromAddress\": \"sender@gmail.com\",\n" +
                "  \"toAddress\": \"receiver@gmail.com\",\n" +
                "  \"subject\": \"This is my email\",\n" +
                "  \"content\": \"Click here to create a click event: http://www.google.com\"\n" +
                "}";
        try {sendgridRequest = mapper.readValue(json, SendgridRequest.class );} catch (Exception ignore) {}
        assertThat(sendgridRequest).isNotNull();
        assertThat(sendgridRequest.getSenderName()).isEqualTo("me");
        assertThat(sendgridRequest.getContent()).isEqualTo("Click here to create a click event: http://www.google.com");
        assertThat(sendgridRequest.getSubject()).isEqualTo("This is my email");
        assertThat(sendgridRequest.getToAddress()).isEqualTo("receiver@gmail.com");
        assertThat(sendgridRequest.getFromAddress()).isEqualTo("sender@gmail.com");
    }

    @Test
    void json_conversion_bad_json() throws JsonProcessingException {
        String json = "{\n" +
                "  \"senderName\": \"me\",XXXX\n" +
                "  \"fromAddress\": \"sender@gmail.com\",\n" +
                "  \"toAddress\": \"receiver@gmail.com\",\n" +
                "  \"subject\": \"This is my email\",\n" +
                "  \"content\": \"Click here to create a click event: http://www.google.com\"\n" +
                "}";
        assertThrows(Exception.class, () -> mapper.readValue(json, SendgridRequest.class ));

    }

    @Test
    void json_empty_conversion() {
        String json = "{}";
        try {sendgridRequest = mapper.readValue(json, SendgridRequest.class );} catch (Exception ignore) {}
        assertThat(sendgridRequest).isNotNull();
        assertThat(sendgridRequest.getSenderName()).isNull();
        assertThat(sendgridRequest.getContent()).isNull();
        assertThat(sendgridRequest.getSubject()).isNull();
        assertThat(sendgridRequest.getToAddress()).isNull();
        assertThat(sendgridRequest.getFromAddress()).isNull();
    }
}