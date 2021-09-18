package unittest.com.chrislomeli.sendgridmailer.service;

import com.chrislomeli.sendgridmailer.service.SendgridMailer;
import com.chrislomeli.sendgridmailer.service.SendgridRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Client;
import com.sendgrid.Request;
import com.sendgrid.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;


@SpringBootTest(classes = com.chrislomeli.sendgridmailer.SendgridMailerApplication.class)
@ContextConfiguration
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SendGridMailerAltPropsTest {
    static ObjectMapper mapper = new ObjectMapper();

    @Mock
    private Client sendGridClient;

    @Test
    void validates_null_key_returns_500() throws IOException {
        // mock the API
        lenient().when(sendGridClient.api(any(Request.class))).thenReturn(new Response(HttpStatus.OK.value(), "{}", null));

        // create an input sendgridRequest based on the parameterized values passed in and set properties
        var mailer = new SendgridMailer(sendGridClient);

        // call SendgridMailer::send
        var response = mailer.send(
                SendgridRequest.builder()
                        .senderName("any name")
                        .toAddress("any@gmail.com")
                        .fromAddress("any@gmail.com")
                        .subject("Any")
                        .content("any")
                        .build());

        // verify the expected stats code
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());

        // if the status code is OK, then we should have caled the API
        //   - otherwise we should have returned early and NOT called the API
        verifyNoInteractions(sendGridClient);
    }

}