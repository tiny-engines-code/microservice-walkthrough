package com.chrislomeli.sendgridmailer.service;

import com.chrislomeli.sendgridmailer.config.Generated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendgridRequest  {
    @Schema(description = "Sender name", required = true)
    String senderName;

    @Schema(description = "Email address of the sender", example = "me@gmail.com", required = true)
    @Email
    String fromAddress;

    @Schema(description = "Email address of the recipient", example = "you@gmail.com", required = true)
    @Email
    String toAddress;


    @Schema(description = "Email subject", required = true)
    @NotBlank
    String subject;

    @Schema(description = "Email body", required = false)
    String content;


    @Schema(description = "Properties that Sendgrid will return to us in event activity", required = false)
    private Map<String, String> customArgs;
}
