package integration.com.chrislomeli.sendgridmailer.util;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Map;

public class WireMockInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
//        WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().port(3000));
        WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
        wireMockServer.start();

        configurableApplicationContext
                .getBeanFactory()
                .registerSingleton("wireMockServer", wireMockServer);

        configurableApplicationContext.addApplicationListener(applicationEvent -> {
            if (applicationEvent instanceof ContextClosedEvent) {
                wireMockServer.stop();
            }
        });

        TestPropertyValues
                .of(Map.of("todo_base_url", "http://localhost:" + wireMockServer.port()))
                .applyTo(configurableApplicationContext);
    }
}