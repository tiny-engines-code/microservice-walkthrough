package integration.com.chrislomeli.sendgridmailer.integration;

import com.chrislomeli.sendgridmailer.controller.SendgridController;
import com.chrislomeli.sendgridmailer.controller.SendgridHandler;
import com.chrislomeli.sendgridmailer.service.SendgridMailer;
import com.chrislomeli.sendgridmailer.service.SendgridProperties;
import com.chrislomeli.sendgridmailer.service.SendgridRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import integration.com.chrislomeli.sendgridmailer.util.WireMockInitializer;
import com.sendgrid.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;



@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = com.chrislomeli.sendgridmailer.SendgridMailerApplication.class)
@ContextConfiguration(initializers = {WireMockInitializer.class})
@AutoConfigureWebTestClient
class SendgridReturnsIT {

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    SendgridProperties sendgridProperties;

    SendgridMailer mailer;
    String apiKeyValue = "myAPIKey";
    String sdkVersion = "4.7.0";
    String apiVersion = "v3";

    static String validJsonEmail;

    @BeforeAll
    static void init() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        validJsonEmail = mapper.writeValueAsString(SendgridRequest.builder()
                .senderName("me").toAddress("r@gmail.com").fromAddress("you@gmail.com").subject("some subject").content("Hi there!")
                .build());
    }

    @AfterEach
    public void afterEach() {
        this.wireMockServer.resetAll();
    }

    @BeforeEach
    public void setUp() {
        // Boiler plate?
        mailer = new SendgridMailer(new Client(true));  // set to true to get http
        sendgridProperties.setHost("localhost:"+ wireMockServer.port());   // set the connect properties the mailer should use
        sendgridProperties.setApiKeyValue(this.apiKeyValue);
        sendgridProperties.setApiVersion( this.apiVersion );
        sendgridProperties.setSdkVersion( this.sdkVersion );
    }

    static Stream<Integer> statusCodeProvider() {
        return Stream.of(
                Stream.of(200,201,202, 428,429,431),
                IntStream.range(400, 426).boxed(),
                IntStream.range(500, 511).boxed()
        ).flatMap(x -> x);
    }

    @ParameterizedTest
    @MethodSource("statusCodeProvider")
    void handle_valid_email_format_scenario(int expectedStatusValue) throws JsonProcessingException {
        /* --- a valid email format is submitted so that it can get all the way to the Sendgrid client---*/
        var thisEmail = validJsonEmail;

        /* --- We'll have the Wiremock return the specific http code that was passed in for this iteration ---*/
        var expectedStatus = HttpStatus.valueOf(expectedStatusValue);

        /* --- We configure Wiremock to mock the real SendGrid API---*/
        var matchUrl = "/" + this.apiVersion + "/" + "mail/send";
        this.wireMockServer.stubFor(
                post(urlMatching(matchUrl))
                        .willReturn(aResponse()
                                .withStatus(expectedStatus.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(responseBody(expectedStatus.value()))));

        // We call the Controller at the very bgining of the stack
        SendgridController sendgridController = new SendgridController(new SendgridHandler(mailer));
        var response = sendgridController.handleRequest(thisEmail);

        /* --- THEN we should recieve the expected status and pass it back up the stack to the user ---*/
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    }

    String responseBody(int status) {
        return String.format("{\"statusCode\":%d,\"body\":\"{\\\"result\\\":" +
                "[{\\\"message\\\":\\\"Lorem Ipsum, Blah, Blah\\\",\\\"field\\\":null,\\\"help\\\":null}]}\"," +
                "\"headers\":{\"Strict-Transport-Security\":\"max-age=600; includeSubDomains\",\"Server\":\"nginx\"," +
                "\"Access-Control-Allow-Origin\":\"https://sendgrid.api-docs.io\",\"Access-Control-Allow-Methods\":" +
                "\"POST\",\"Connection\":\"keep-alive\",\"X-No-CORS-Reason\":\"https://sendgrid.com/docs/Classroom/Basics/API/cors.html\"," +
                "\"Content-Length\":\"116\",\"Access-Control-Max-Age\":\"600\",\"Date\":\"Mon, 16 Aug 2021 12:42:04 GMT\"," +
                "\"Access-Control-Allow-Headers\":\"Authorization, Content-Type, On-behalf-of, x-sg-elas-acl\",\"Content-Type\":" +
                "\"application/json\"}}", status );
    }
}