package acceptance.java.com.chrislomeli.sendgridmailer;

import com.chrislomeli.sendgridmailer.controller.SendgridController;
import com.chrislomeli.sendgridmailer.controller.SendgridHandler;
import com.chrislomeli.sendgridmailer.service.SendgridMailer;
import com.chrislomeli.sendgridmailer.service.SendgridProperties;
import com.chrislomeli.sendgridmailer.service.SendgridRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.sendgrid.Client;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.Integer.parseInt;
import static org.assertj.core.api.Assertions.assertThat;

public class HandlesSendgridResponses {
        static ObjectMapper mapper = new ObjectMapper();

        @Autowired
        private WireMockServer wireMockServer;

        @Autowired
        SendgridProperties sendgridProperties;

        List<Integer> testSuite;

        SendgridMailer mailer;
        String apiKeyValue = "myAPIKey";
        String sdkVersion = "4.7.0";
        String apiVersion = "v3";
        static String validJsonEmail;

        @Given("I am sending any well-formated email")
        public void i_am_sending_any_well_formated_email() throws JsonProcessingException {
            // Write code here that turns the phrase above into concrete actions
                ObjectMapper mapper = new ObjectMapper();
                validJsonEmail = mapper.writeValueAsString(SendgridRequest.builder()
                        .senderName("me").toAddress("r@gmail.com").fromAddress("you@gmail.com").subject("some subject").content("Hi there!")
                        .build());

                mailer = new SendgridMailer(new Client(true));  // set to true to get http
                sendgridProperties.setHost("localhost:"+ wireMockServer.port());   // set the connect properties the mailer should use
                sendgridProperties.setApiKeyValue(this.apiKeyValue);
                sendgridProperties.setApiVersion( this.apiVersion );
                sendgridProperties.setSdkVersion( this.sdkVersion );
        }

        @When("The API returns any code")
        public void the_api_returns_any_code(io.cucumber.datatable.DataTable dataTable) {
                this.testSuite = new ArrayList<>();
                List<List<String>> rows = dataTable.asLists(String.class);

                for (List<String> columns : rows) {
                        var p = columns.get(0).split("-");
                        var startvalue = parseInt(p[0]);
                        var endvalue = (p.length > 1) ? parseInt(p[1]) : startvalue;
                        for (int i=startvalue; i < endvalue; i++)
                                this.testSuite.add(i);
                }
        }


        @Then("I should receive the same code")
        public void i_should_receive_the_same_code() {
                this.testSuite.forEach(this::apiReturnsCode);
        }

        void apiReturnsCode(int expectedStatusValue)  {
                /* --- GIVEN a valid email format is submitted ---*/
                var thisEmail = validJsonEmail;

                /* --- WHEN the expected server response is {triggered.nike.com} ---*/
                var expectedStatus = HttpStatus.valueOf(expectedStatusValue);

                /* --- AND We call the service ---*/
                var matchUrl = "/" + this.apiVersion + "/" + "mail/send";
                this.wireMockServer.stubFor(
                        post(urlMatching(matchUrl))
                                .willReturn(aResponse()
                                        .withStatus(expectedStatus.value())
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                        .withBody(responseBody(expectedStatus.value()))));

                SendgridController sendgridController = new SendgridController(new SendgridHandler(mailer));
                var response = sendgridController.handleRequest(thisEmail);

                /* --- THEN we will recieve expected status ---*/
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
