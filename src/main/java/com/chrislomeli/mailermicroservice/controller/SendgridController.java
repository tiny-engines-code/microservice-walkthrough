package com.chrislomeli.mailermicroservice.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example endpoint that returns version information about the application.
 * Also, provides a test interface for the actual functionality
 */
@RestController
@RequestMapping("/email/v2")
public class SendgridController {

    final SendgridHandler controllerFacade;

    public SendgridController(SendgridHandler controllerFacade) {
        this.controllerFacade = controllerFacade;
    }

    /**
     * Send a json notification payload directly to the SendGridMessageHandler
     */
    @PostMapping(path = "/send")
    public ResponseEntity<String> handleRequest(@RequestBody String mailRequest)  {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        // create
        try {
            String result = controllerFacade.requestHandler(mailRequest);
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .headers(responseHeaders)
                    .body(e.getLocalizedMessage());
        }
    }

}
