package com.chrislomeli.mailermicroservice;

import com.chrislomeli.mailermicroservice.controller.SendgridController;
import com.chrislomeli.mailermicroservice.controller.SendgridHandler;
import com.chrislomeli.mailermicroservice.service.SendgridMailer;
import com.chrislomeli.mailermicroservice.service.SendgridProperties;
import com.chrislomeli.mailermicroservice.service.SendgridRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
//import io.restassured.RestAssured;
//import io.restassured.response.Response;
//import org.junit.Assert;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.sendgrid.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {WireMockInitializer.class})
@AutoConfigureWebTestClient
class WireMockJUnit5Test {

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    SendgridProperties sendgridProperties;

    String apiKeyValue = "myAPIKey";
    String sdkVersion = "4.7.0";
    String apiVersion = "v3";
    String host = "api.sendgrid.com";

//    @Autowired
//    private WebTestClient webTestClient;

    @LocalServerPort
    private Integer port;



//    @BeforeEach
//    public void setup() {
//        wireMockServer = new WireMockServer(8090);
//        wireMockServer.start();
//        setupStub();
//    }

    @AfterEach
    public void afterEach() {
        this.wireMockServer.resetAll();
//        wireMockServer.stop();
    }


//    public void setupStub() {
//        wireMockServer.stubFor(WireMock.get( WireMock.urlEqualTo("/an/endpoint"))
//                .willReturn(aResponse().withHeader("Content-Type", "text/plain")
//                        .withStatus(200)
//                        .withBodyFile("json/glossary.json")));
//    }

    @Test
    void testGetAllTodosShouldReturnDataFromClient() throws JsonProcessingException {
        var expectedStatus = HttpStatus.BANDWIDTH_LIMIT_EXCEEDED;
        this.wireMockServer
                .stubFor(
                    post(urlMatching("/v3/mail/send"))
                        .willReturn(aResponse()
                                .withStatus(expectedStatus.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("[{\"userId\": 1,\"id\": 1,\"title\": \"Learn Spring Boot 3.0\", \"completed\": false}," +
                                        "{\"userId\": 1,\"id\": 2,\"title\": \"Learn WireMock\", \"completed\": true}]"))
        );

        ObjectMapper mapper = new ObjectMapper();
        var content = mapper.writeValueAsString(SendgridRequest.builder()
                .senderName("me").toAddress("r@gmail.com").fromAddress("you@gmail.com").subject("some subject").content("Hi there!")
                .build());



        SendgridMailer mailer = new SendgridMailer(new Client(true));
        var wmPort = wireMockServer.port();
        mailer.setHost("localhost:"+wmPort);
        mailer.setApiKeyValue(this.apiKeyValue);
        mailer.setApiVersion( this.apiVersion );
        mailer.setSdkVersion( this.sdkVersion );

        sendgridProperties.setHost("localhost:"+wmPort);
        sendgridProperties.setApiKeyValue(this.apiKeyValue);
        sendgridProperties.setApiVersion( this.apiVersion );
        sendgridProperties.setSdkVersion( this.sdkVersion );

        SendgridController sendgridController = new SendgridController(new SendgridHandler(mailer));


        var r = sendgridController.handleRequest(content);

        System.out.println(r.getStatusCode());
        assertThat(r.getStatusCode()).isEqualTo(expectedStatus);

    }
}