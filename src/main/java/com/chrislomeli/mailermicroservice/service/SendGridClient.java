package com.chrislomeli.mailermicroservice.service;

import com.sendgrid.Client;
import com.sendgrid.Request;
import com.sendgrid.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * class SendGridClient
 * Wrapper class for http call to SendGrid - this interface can be mocked for testing
 */
@Slf4j
@Component
public class SendGridClient {

    private static final String VERSION = "4.7.0";
    private static final String HOST = "api.sendgrid.com";
    private static final String USER_AGENT = "sendgrid/" + VERSION + ";java";

    private String host = HOST;
    private String apiVersion = VERSION;
    private Client client;
    private Map<String, String> requestHeaders;

    /**
     * Construct a new API wrapper.
     */
    public SendGridClient() {
        this.initialize();
        this.client = new Client();
    }

    /**
     * Initialize the client.
     */
    public void initialize() {
        this.apiVersion = "v3";
        this.requestHeaders = new HashMap<>();
        this.requestHeaders.put("User-Agent", USER_AGENT);
        this.requestHeaders.put("Accept", "application/json");
    }

    /**
     * Class api sets up the request to the Twilio SendGrid API, this is main interface.
     */
    public Response sendMail(final Request request, String auth) {
        final Request req = new Request();
        this.requestHeaders.put("Authorization", "Bearer " + auth);

        req.setMethod(request.getMethod());
        req.setBaseUri(this.host);
        req.setEndpoint("/" + apiVersion + "/" + request.getEndpoint());
        req.setBody(request.getBody());

        for (final Map.Entry<String, String> header : this.requestHeaders.entrySet()) {
            req.addHeader(header.getKey(), header.getValue());
        }

        for (final Map.Entry<String, String> queryParam : request.getQueryParams().entrySet()) {
            req.addQueryParam(queryParam.getKey(), queryParam.getValue());
        }

             try {

                 return client.api(req);

            } catch (IOException e) {
               return new Response(HttpStatus.BAD_GATEWAY.value(), e.getMessage(), req.getHeaders());
            }
    }

    public Response timeOutResponse() {
        var r = new Response();
        r.setStatusCode(HttpStatus.REQUEST_TIMEOUT.value());
        r.setBody("{\"reason\": \"Circuit-Breaker timed out\"}");
        return r;
    }
}
