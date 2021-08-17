package com.chrislomeli.mailermicroservice.ut.controller;

import com.chrislomeli.mailermicroservice.ut.service.SendgridMailer;
import com.chrislomeli.mailermicroservice.ut.service.SendgridProperties;
import com.chrislomeli.mailermicroservice.ut.service.SendgridRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Client;
import com.sendgrid.Request;
import com.sendgrid.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class SendgridHandlerTest {
    String apiKeyValue = "myAPIKey";
    String sdkVersion = "4.7.0";
    String apiVersion = "v3";
    String host = "api.sendgrid.com";

    static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    SendgridProperties sendgridProperties;

    static Stream<Integer> statusCodeProvider() {
        return Stream.of(
                Stream.of(200,201,202),
                IntStream.range(400, 451).boxed(),
                IntStream.range(500, 511).boxed()
        ).flatMap(x -> x);
    }

    /* --------------------------------------
     * SendgridHandler Unit tests
     * Inputs:
     *      - json string -- null, good bad conversion
     * Operations:
     *      - json mapper
     *
     * Outputs and returns
     *      - Response = SendgridMailer.send() -- http codes, null, th
     *
     */

    /*----------------------------------
       handle json input
     */
    @Test
    void test_handle_null_input_returns_400() throws Exception {
        var response = new SendgridHandler(new SendgridMailer()).requestHandler(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void test_handle_bad_json_input_returns_400() throws Exception {
        var response = new SendgridHandler(new SendgridMailer()).requestHandler("{ \"name\":\"jo}");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /*----------------------------------
       SendGridMailer.send() return values
     */
    @ParameterizedTest // all status codes
    @MethodSource("statusCodeProvider")
    void handles_all_return_codes(Integer intCode) throws Exception {
        var sendGridClient = mock(Client.class);
        when(sendGridClient.api(any(Request.class))).thenReturn(new Response(intCode, "{}", null));

        var mailer = new SendgridMailer(sendGridClient);
        sendgridProperties.setHost("mocked");
        sendgridProperties.setApiKeyValue(this.apiKeyValue);
        sendgridProperties.setApiVersion( this.apiVersion );
        sendgridProperties.setSdkVersion( this.sdkVersion );

        var response = new SendgridHandler(mailer).requestHandler(createRequest());
        assertEquals(response.getStatusCode(), intCode);
    }

    @Test // throws
    void handles_requestHandler_null() throws Exception {
        var sendGridMailer = mock(SendgridMailer.class);
        when(sendGridMailer.send(any(SendgridRequest.class))).thenThrow(new RuntimeException("Badd JuJu"));
        var response = new SendgridHandler(sendGridMailer).requestHandler(createRequest());
        assertAll(
                () -> assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
                () -> assertEquals("Badd JuJu", response.getBody())
        );
    }

    @Test // throws
    void handles_requestHandler_exception() throws Exception {
        var sendGridMailer = mock(SendgridMailer.class);
        when(sendGridMailer.send(any(SendgridRequest.class))).thenReturn(null);
        var response = new SendgridHandler(sendGridMailer).requestHandler(createRequest());
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /*----------------------------------
       Helpers
     */
    private static String createRequest() throws JsonProcessingException {
                return mapper.writeValueAsString(SendgridRequest.builder()
                        .senderName("sender").toAddress("to@gmail.com").fromAddress("from@gmail.com").subject("subject").content("body")
                        .build());

    }
}