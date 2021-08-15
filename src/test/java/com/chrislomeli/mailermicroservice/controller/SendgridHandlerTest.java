package com.chrislomeli.mailermicroservice.controller;

import com.chrislomeli.mailermicroservice.service.SendgridMailer;
import com.chrislomeli.mailermicroservice.service.SendgridRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Client;
import com.sendgrid.Request;
import com.sendgrid.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SendgridHandlerTest {
    static ObjectMapper mapper = new ObjectMapper();

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
        var client = mock(Client.class);
        when(client.api(any(Request.class))).thenReturn(new Response(intCode, "{}", null));
        var response = new SendgridHandler(new SendgridMailer(client)).requestHandler(createRequest());
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