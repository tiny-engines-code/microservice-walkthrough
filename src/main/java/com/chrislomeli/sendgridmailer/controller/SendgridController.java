package com.chrislomeli.sendgridmailer.controller;

import com.chrislomeli.sendgridmailer.service.SendgridRequest;
import com.sendgrid.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
     * Send a json notification payload to the SendGridMessageHandler
     */
    @Operation(summary = "Send a new email",
            description = "See the Sendgrid documentation for the Response object",
            tags = { "send" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request was processed (the actual return code is in the Response::statusCode") })

    @PostMapping(value = "/send", consumes = { "application/json" })
    public ResponseEntity<Response> handleRequest(
            @Parameter(description="Simple Email request", required=true, schema=@Schema(implementation = SendgridRequest.class))
            @RequestBody String mailRequest) {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        // create
        try {
            Response result = controllerFacade.requestHandler(mailRequest);
            return ResponseEntity.status(result.getStatusCode())
                    .headers(responseHeaders)
                    .body(result);
        } catch (Exception e) {
            return  ResponseEntity.internalServerError()
                    .headers(responseHeaders)
                    .body( new Response(HttpStatus.INTERNAL_SERVER_ERROR.value(), "<field list>", Map.of("Content-type", "application/json")));
        }
    }

}
