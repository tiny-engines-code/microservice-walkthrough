package com.chrislomeli.mailermicroservice.service;

import lombok.Data;

import java.util.Map;

@Data
public class SendgridRequest {
    String senderName;
    String fromAddress;
    String toAddress;
    String subject;
    String content;
    Map<String, String> customArgs;
}
