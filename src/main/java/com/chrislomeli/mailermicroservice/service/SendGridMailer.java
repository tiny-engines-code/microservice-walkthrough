package com.chrislomeli.mailermicroservice.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * class SendGridMailer
 * Main WORKER class for marshalling, converting, and sending to SendGrid
 * entry point is method sendGridEmail()
 * which sends the mail and passes back a MessageHandlerResult
 */
@Slf4j
@Component
public class SendGridMailer {


    @Value("${sendgrid.auth.key:no-auth}")
    String apiKeyValue;

    private final SendGridClient emailConnection;

    public SendGridMailer(SendGridClient emailConnection) {
        this.emailConnection = emailConnection;
    }

    /**
     * Convert an ObservableRequest to a SendGrid Mail object
     */
    public Response send(SendgridRequest mailRequest) throws IOException {
        Mail mailer = createSendGridMail(mailRequest);
        final Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mailer.build());

        return  emailConnection.sendMail(request, apiKeyValue);
    }

    public Response timeOutResponse()  {
        var r = new Response();
        r.setStatusCode(HttpStatus.REQUEST_TIMEOUT.value());
        r.setBody("{\"reason\": \"Circuit-Breaker timed out\"}");
        return r;
    }

    public Mail createSendGridMail(SendgridRequest mail) {
        /* initialize the SendGridMail that we pass back */
        Mail sendgrid = new Mail();

        try {
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

            mail.customArgs.entrySet().stream().forEach(entry ->
                    sendgrid.addCustomArg(entry.getKey(), entry.getValue()));

            /* Tracking */
            addTrackingSettings(sendgrid);

        } catch (Exception ignore) { // todo - don't ignore
        }

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
