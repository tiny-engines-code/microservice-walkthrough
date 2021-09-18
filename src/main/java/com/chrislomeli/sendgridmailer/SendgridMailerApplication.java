package com.chrislomeli.sendgridmailer;

import com.chrislomeli.sendgridmailer.config.Generated;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class SendgridMailerApplication {

    @Generated
    public static void main(String[] args) {
        SpringApplication.run(SendgridMailerApplication.class, args);
    }
}
