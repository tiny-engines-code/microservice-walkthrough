package acceptance.java.com.chrislomeli.sendgridmailer;

import integration.com.chrislomeli.sendgridmailer.util.WireMockInitializer;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = com.chrislomeli.sendgridmailer.SendgridMailerApplication.class)
@ContextConfiguration(initializers = {WireMockInitializer.class})
@AutoConfigureWebTestClient
@CucumberContextConfiguration
public class CucumberSpringConfiguration {

    @Autowired
    protected TestRestTemplate restTemplate;

}