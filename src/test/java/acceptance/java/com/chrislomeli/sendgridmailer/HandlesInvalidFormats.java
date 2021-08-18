package acceptance.java.com.chrislomeli.sendgridmailer;

import com.chrislomeli.sendgridmailer.controller.SendgridController;
import com.chrislomeli.sendgridmailer.controller.SendgridHandler;
import com.chrislomeli.sendgridmailer.service.SendgridMailer;
import com.chrislomeli.sendgridmailer.service.SendgridProperties;
import com.chrislomeli.sendgridmailer.service.SendgridRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Client;
import com.sendgrid.Request;
import com.sendgrid.Response;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

public class HandlesInvalidFormats extends CucumberSpringConfiguration{

    static ObjectMapper mapper = new ObjectMapper();

    @Mock
    private Client sendGridClient;

    @Autowired
    SendgridProperties sendgridProperties;

    List<Pair<Integer, SendgridRequest>> testSuite;

    SendgridMailer mailer;
    String apiKeyValue = "myAPIKey";
    String sdkVersion = "4.7.0";
    String apiVersion = "v3";

    @Given("the API is available")
    public void the_api_is_available() {
        mailer = new SendgridMailer(new Client(true));  // set to true to get http
        sendgridProperties.setHost("mocked");
        sendgridProperties.setApiKeyValue(this.apiKeyValue);
        sendgridProperties.setApiVersion(this.apiVersion);
        sendgridProperties.setSdkVersion(this.sdkVersion);
    }

    @When("I am sending valid and invalid emails")
    public void i_am_sending_the_following_emails(io.cucumber.datatable.DataTable dataTable) {
        this.testSuite = new ArrayList<>();
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {

            var sg = SendgridRequest.builder()
                    .senderName(columns.get("sender"))
                    .toAddress(columns.get("to"))
                    .fromAddress(columns.get("from"))
                    .subject(columns.get("subject"))
                    .content(columns.get("body"))
                    .build();

            var status = Integer.parseInt(columns.get("expected"));
            this.testSuite.add(Pair.of(status, sg));
        }
    }

    @Then("Validation errors should be caught before being sent to the API")
    public void then_i_should_receive_the_expected_reponses() {
        this.testSuite.forEach(x -> handle_invalid_formats(x.getLeft(), x.getRight()));
    }


    void handle_invalid_formats(int expectedStatus, SendgridRequest sendgridRequest) {
        try {
            var thisEmail = mapper.writeValueAsString(sendgridRequest);

            // mock the API - this should never be called - but if it is - that means it passed our validat
            lenient().when(sendGridClient.api(any(Request.class))).thenReturn(new Response(HttpStatus.OK.value(), "{}", null));
            // send the email
            SendgridController sendgridController = new SendgridController(new SendgridHandler(mailer));
            var response = sendgridController.handleRequest(thisEmail);

            // --- THEN it should return the {correct} result ---
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(expectedStatus));

            // --- Sendgrid should not be called if we had a validation error
            if (expectedStatus != HttpStatus.OK.value()) {
                verifyNoInteractions(sendGridClient);
            }

        } catch (Exception ignored) {

        }
    }

}
