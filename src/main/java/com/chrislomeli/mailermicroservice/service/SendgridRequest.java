package com.chrislomeli.mailermicroservice.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendgridRequest {
    String senderName;
    String fromAddress;
    String toAddress;
    String subject;
    String content;
    Map<String, String> customArgs;
}
