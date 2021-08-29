package unittest.com.chrislomeli.sendgridmailer.service;

import com.chrislomeli.sendgridmailer.controller.SendgridHandler;
import com.chrislomeli.sendgridmailer.service.SendgridMailer;
import com.chrislomeli.sendgridmailer.service.SendgridProperties;
import com.chrislomeli.sendgridmailer.service.SendgridRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.sendgrid.Client;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;


@SpringBootTest(classes = com.chrislomeli.sendgridmailer.SendgridMailerApplication.class)
@ContextConfiguration
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class SendGridMailerTest {

    String apiKeyValue = "myAPIKey";
    String sdkVersion = "4.7.0";
    String apiVersion = "v3";
    String host = "api.sendgrid.com";

    static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    SendgridProperties sendgridProperties;

    @Mock
    private Client sendGridClient;

    @Captor
    ArgumentCaptor<Request> apiRequestCaptor;

    private static Stream<Arguments> payloadProvider() {
        return Stream.of(
                Arguments.of("happy_path", HttpStatus.OK, "sender", "from@gmail.com", "to@gmail.com", "subjectline", "body", Map.of("cheese", "grater")),
                Arguments.of("bad_from_address", HttpStatus.BAD_REQUEST, "sender", "from.BAD.com", "to@gmail.com", "subjectline", "body", Map.of("cheese", "grater")),
                Arguments.of("bad_to_address", HttpStatus.BAD_REQUEST, "sender", "from@gmail.com", "to.BAD.com", "subjectline", "body", Map.of("cheese", "grater")),
                Arguments.of("empty_subject", HttpStatus.BAD_REQUEST, "sender", "from@gmail.com", "to@gmail.com", "", "body", Map.of("cheese", "grater")),
                Arguments.of("empty_body", HttpStatus.OK, "sender", "from@gmail.com", "to@gmail.com", "subject", "", Map.of("cheese", "grater")),
                Arguments.of("null_from_address", HttpStatus.BAD_REQUEST, "sender", null, "to@gmail.com", "subjectline", "body", Map.of("cheese", "grater")),
                Arguments.of("null_to_address", HttpStatus.BAD_REQUEST, "sender", "from@gmail.com", null, "subjectline", "body", Map.of("cheese", "grater")),
                Arguments.of("null_subject", HttpStatus.BAD_REQUEST, "sender", "from@gmail.com", "to@gmail.com", null, "body", Map.of("cheese", "grater")),
                Arguments.of("null_body", HttpStatus.OK, "sender", "from@gmail.com", "to@gmail.com", "subject", null, Map.of("cheese", "grater")),
                Arguments.of("null_args", HttpStatus.OK, "sender", "from@gmail.com", "to@gmail.com", "subject", "body", null)
        );
    }

    private static Stream<Arguments> goodheartedMissives() {
        return Stream.of(
                Arguments.of("happy_path", "sender", "from@gmail.com", "to@gmail.com", "subjectline", "body", Map.of("name", "chandler", "city", "san jose")),
                Arguments.of("empty_body",  "sender", "from@gmail.com", "to@gmail.com", "subject", "", Map.of("student", "ben jones")),
                Arguments.of("null_body",  "sender", "from@gmail.com", "to@gmail.com", "subject", null, Map.of("key-value", "!2@*()___")),
                Arguments.of("null_args", "sender", "from@gmail.com", "to@gmail.com", "subject", "body", null)
        );
    }

    /*-------------------------------------------------
      Handle payload buiness scenarios
        missing fields, bad formatting, etc.
     */
    @ParameterizedTest(name = "{index} => ''{0}'' - ''{1}''")
    @MethodSource("payloadProvider")
    void validates_requests_and_return_with_code_or_call_api(String message, HttpStatus expectedStatus, String sender, String from, String to, String subject, String body, Map<String, String> custom) throws IOException {
        // mock the API
        lenient().when(sendGridClient.api(any(Request.class))).thenReturn(new Response(HttpStatus.OK.value(), "{}", null));

        // create an input sendgridRequest based on the parameterized values passed in and set properties
        var mailer = new SendgridMailer(sendGridClient);
        sendgridProperties.setHost("mocked");
        sendgridProperties.setApiKeyValue(this.apiKeyValue);
        sendgridProperties.setApiVersion( this.apiVersion );
        sendgridProperties.setSdkVersion( this.sdkVersion );

        // call SendgridMailer::send
        var response = mailer.send(
                SendgridRequest.builder()
                        .senderName(sender)
                        .toAddress(to)
                        .fromAddress(from)
                        .subject(subject)
                        .content(body)
                        .customArgs(custom)
                        .build());

        // verify the expected stats code
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus.value());

        // if the status code is OK, then we should have caled the API
        //   - otherwise we should have returned early and NOT called the API
        if (expectedStatus == HttpStatus.OK) {
            verify(sendGridClient).api(apiRequestCaptor.capture());
            Request request = apiRequestCaptor.getValue();
            assertThat(request.getMethod()).isEqualTo(Method.POST);
            assertThat(request.getBaseUri()).isNotNull();
            assertThat(request.getEndpoint()).isEqualTo(String.format("/%s/mail/send", this.apiVersion));
        } else {
            verifyNoInteractions(sendGridClient);
        }
    }

    /*-------------------------------------------------
      Handle Client API throws
     */
    @Test
    void test_client_send_throws() throws Exception {
        when(sendGridClient.api(any(Request.class))).thenThrow(new RuntimeException("Badd JuJu"));
        var payload = createPayload("sender", "from@gmail.com", "to@gmail.com", "subject", "body", null);
        var response = new SendgridHandler(new SendgridMailer(sendGridClient)).requestHandler(payload);

        assertAll(
                () -> assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
                () -> assertEquals("Badd JuJu", response.getBody())
        );
    }

    /*------------------------------------------------------
      Check that when the request makes it to the sendgrid API
      all of the date is correct
     */
    @ParameterizedTest(name = "{index} => ''{0}''")
    @MethodSource("goodheartedMissives")
    void api_request_has_correct_data(String message, String sender, String from, String to, String subject, String body, Map<String, String> custom) throws IOException {
        var sendgridRequest = SendgridRequest.builder()
                .senderName(sender)
                .toAddress(to)
                .fromAddress(from)
                .subject(subject)
                .content(body)
                .customArgs(custom)
                .build();

        lenient().when(sendGridClient.api(any(Request.class))).thenReturn(new Response(HttpStatus.OK.value(), "{}", null));
        var mailer = new SendgridMailer(sendGridClient);

        sendgridProperties.setHost("mocked");
        sendgridProperties.setApiKeyValue(this.apiKeyValue);
        sendgridProperties.setApiVersion( this.apiVersion );
        sendgridProperties.setSdkVersion( this.sdkVersion );

        var response = mailer.send(sendgridRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());

        verify(sendGridClient).api(apiRequestCaptor.capture());

        Request request = apiRequestCaptor.getValue();
        assertThat(request.getMethod()).isEqualTo(Method.POST);
        assertThat(request.getBaseUri()).isNotNull();
        assertThat(request.getEndpoint()).isEqualTo(String.format("/%s/mail/send", this.apiVersion));
        assertThat(sendgridRequest.getSubject()).isEqualTo(JsonPath.read(request.getBody(), "$.subject"));
        assertThat(sendgridRequest.getFromAddress()).isEqualTo(JsonPath.read(request.getBody(), "$.from.email"));
        assertThat(sendgridRequest.getToAddress()).isEqualTo(JsonPath.read(request.getBody(), "$.personalizations[0].to[0].email"));
        if (sendgridRequest.getContent() != null && !sendgridRequest.getContent().isEmpty())
            assertThat(sendgridRequest.getContent()).isEqualTo(JsonPath.read(request.getBody(), "$.content[0].value"));
        if (sendgridRequest.getCustomArgs() != null && sendgridRequest.getCustomArgs().size() > 0) {
            var ca = (LinkedHashMap<String, String>) JsonPath.read(request.getBody(), "$.custom_args");
            for (Map.Entry<String, String> elem : sendgridRequest.getCustomArgs().entrySet())
                assertThat(ca).containsEntry(elem.getKey(), elem.getValue());
        }
    }


    // Utility method
    private String createPayload(String sender, String from, String to, String subject, String body, Map<String, String> customArgs) throws JsonProcessingException {
        return mapper.writeValueAsString(
                SendgridRequest.builder()
                        .senderName(sender).toAddress(to).fromAddress(from).subject(subject).content(body).customArgs(customArgs)
                        .build()
        );
    }
}