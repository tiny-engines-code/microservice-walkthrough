package unittest.com.chrislomeli.sendgridmailer.controller;

import com.chrislomeli.sendgridmailer.controller.SendgridHandler;
import com.chrislomeli.sendgridmailer.service.SendgridRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.core.io.buffer.DataBufferUtils.matcher;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest(classes = com.chrislomeli.sendgridmailer.SendgridMailerApplication.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class SendgridControllerTest {
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SendgridHandler sendgridHandler;

    static Stream<Integer> statusCodeProvider() {
        return Stream.of(
                Stream.of(200,201,202),
                IntStream.range(400, 451).boxed(),
                IntStream.range(500, 511).boxed()
        ).flatMap(x -> x);
    }

    static String content;

    @BeforeAll
    static void setupGlobals() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        content = mapper.writeValueAsString(SendgridRequest.builder()
                .senderName("me").toAddress("r@gmail.com").fromAddress("you@gmail.com").subject("some subject").content("Hi there!")
                .build());
    }

    /*------------------------------------------
      Controller::send inputs:
         - handle good and bad url
         - handle correct mothe (POST) and bad methods
         - Handle good, bad, null Json String

      Sendgridhandler responses
        - Valid Response object
        - null
        - exception
     */

    /*----------------------------------------------
      inputs
     */
    @Test
    void handles_bad_json_format() throws Exception {
        var statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        mockMvc.perform(MockMvcRequestBuilders.post("/email/v2/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"bad }")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertEquals("Status", statusCode.value(), result.getResponse().getStatus()));
    }

    @Test
    void handles_bad_method() throws Exception {
        var statusCode = HttpStatus.METHOD_NOT_ALLOWED;
        mockMvc.perform(MockMvcRequestBuilders.get("/email/v2/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"bad }")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertEquals("Status", statusCode.value(), result.getResponse().getStatus()));
    }

    @Test
    void handles_bad_url() throws Exception {
        var statusCode = HttpStatus.NOT_FOUND;
        mockMvc.perform(MockMvcRequestBuilders.post("/email/v1/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"bad }")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertEquals("Status", statusCode.value(), result.getResponse().getStatus()));
    }

    /*-----------------------------------------------------------------
      call handler facade
      - handle all ststus codes - maybe overkill but fast and cheap
      - handle exception
      - handle null
     */
    @ParameterizedTest (name = "{index} handle status code ''{0}''") // Handler responses
    @MethodSource("statusCodeProvider")
    void handes_all_status_codes(int statusCode) throws Exception {
        Mockito.when(sendgridHandler.requestHandler(anyString())).thenReturn( new Response(statusCode, null, null) );
        mockMvc.perform(MockMvcRequestBuilders.post("/email/v2/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertEquals("Status", statusCode, result.getResponse().getStatus()));
    }

    @Test  // Handles exceptions
    void handles_sendgrid_handle_exceptions() throws Exception {
        var expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        Mockito.when(sendgridHandler.requestHandler(anyString())).thenThrow(new RuntimeException("Bad juju"));
        mockMvc.perform(MockMvcRequestBuilders.post("/email/v2/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertEquals("Status", expectedStatus.value(), result.getResponse().getStatus()));
    }

    @Test  // Handles null
    void handles_sendgrid_handle_null_return() throws Exception {
        var expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        Mockito.when(sendgridHandler.requestHandler(anyString())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.post("/email/v2/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertEquals("Status", expectedStatus.value(), result.getResponse().getStatus()));
    }

    @Test
    void handles_empty_json() throws Exception {
        var expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        mockMvc.perform(MockMvcRequestBuilders.post("/email/v2/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertEquals("Status", expectedStatus.value(), result.getResponse().getStatus()));
    }


}