package com.chrislomeli.mailermicroservice.service;

import com.sendgrid.Client;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * class SendGridMailer
 * Main WORKER class for marshalling, converting, and sending to SendGrid
 * entry point is method sendGridEmail()
 * which sends the mail and passes back a MessageHandlerResult
 */
@Slf4j
@Component
public class SendgridMailer {

    @Value("${sendgrid.auth.key:noauth}")
    String apiKeyValue;

    @Value("${sendgrid.api.version:4.7.0}")
    String sdkVersion;

    @Value("${sendgrid.version:v3}")
    String apiVersion;

    @Value("${sendgrid.host:api.sendgrid.com}")
    String host;

    private final Client client;

    public SendgridMailer() {
        this.client = new Client();
    }
    public SendgridMailer(Client injectClient) {
        this.client = injectClient;
    }

    /* simple for a textbook implementation */
    Predicate<String> validEmail = s -> EmailValidator.getInstance().isValid(s);
    Predicate<String> validRequired = s -> s != null && !s.isEmpty();

    public boolean validRequest(SendgridRequest sendgridRequest) {
        return validRequired.test(sendgridRequest.getSenderName()) &&
                validRequired.test(sendgridRequest.getSubject()) &&
                validEmail.test(sendgridRequest.getFromAddress()) &&
                validEmail.test(sendgridRequest.getToAddress());
    }


    /**
     * Convert user request to a SendGrid Mail object
     */
    public Response send(SendgridRequest mailRequest) throws IOException {

        // simplistic validation for this example
        if (!validRequest(mailRequest))
            return new Response(HttpStatus.BAD_REQUEST.value(), "<field list>", Map.of("Content-type", "application/json"));

        // Create a SendGrid Mail object
        Mail mailer = createSendGridMail(mailRequest);

        // Create a SendGrid Request
        final Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("/" + apiVersion + "/" + "mail/send");
        request.setBody(mailer.build());
        request.setBaseUri(this.host);
        request.addHeader("User-Agent", "sendgrid/" + apiVersion + ";java");
        request.addHeader("Authorization", "Bearer " + apiKeyValue);
        request.addHeader("Accept", "application/json");

        // Send to the SendGrid API
        try {
            return client.api(request);
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), request.getHeaders());
        }
    }

    public Mail createSendGridMail(SendgridRequest mail) {
        /* create my request-id for tracing - we are not using it in this example */
        String mailId = UUID.randomUUID().toString();

        /* initialize the SendGridMail that we pass back */
        Mail sendgrid = new Mail();

        /* Sender */
        Email senderEmail = new Email(mail.fromAddress, mail.senderName);
        sendgrid.setFrom(senderEmail);
        sendgrid.setReplyTo(senderEmail);

        /* Recipient */
        Personalization personalization = new Personalization();
        personalization.addTo(new Email(mail.getToAddress()));
        sendgrid.addPersonalization(personalization);

        /* Content */
        sendgrid.setSubject(mail.getSubject());
        sendgrid.addContent(new Content(MediaType.TEXT_HTML.toString(), mail.getContent()));

        /* Call back keys */
        if (mail.customArgs != null && !mail.customArgs.isEmpty()) {
            sendgrid.addCustomArg("mail-id", mailId);  //inject our mailId
            mail.customArgs.forEach(sendgrid::addCustomArg); // inject values passed in
        }

        /* Tracking */
        addTrackingSettings(sendgrid);

        /* Done */
        return sendgrid;
    }

    /**
     *
     */
    private void addTrackingSettings(Mail mail) {
        TrackingSettings trackingSettings = new TrackingSettings();

        // click tracking
        ClickTrackingSetting clickTrackingSetting = new ClickTrackingSetting();
        clickTrackingSetting.setEnable(true);
        clickTrackingSetting.setEnableText(true);
        trackingSettings.setClickTrackingSetting(clickTrackingSetting);
        mail.setTrackingSettings(trackingSettings);

        OpenTrackingSetting openTrackingSetting = new OpenTrackingSetting();
        openTrackingSetting.setEnable(true);
        trackingSettings.setOpenTrackingSetting(openTrackingSetting);

        mail.setTrackingSettings(trackingSettings);

    }

}
