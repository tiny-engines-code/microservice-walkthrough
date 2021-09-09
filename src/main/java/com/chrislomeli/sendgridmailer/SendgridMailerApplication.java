package com.chrislomeli.sendgridmailer;
/*
    SendGrid Mailer Service

    **SendgridController (Controller)**
      1. accepts a json request
      2. passes it to the SendgridHandler
      3. Receives a Response and displays it ot the user

    **SendgridHandler (Handler)**
      1. receives the json request from the controller
      2. validates the json
      3. passes it as a SendgridRequest object to the SendgridMailer
      4. receives the Response from the Mailer and sends it back to the Controller

    **SendgridMailer (Mailer)**
    1. receives the SendgridRequest from the Handler
    2. validates the data
    3. reformats it to a SendGrid Request object
    4. Sends the Request to the SendGrid API
    5. receives the Response from the API and sends it back to the Handler

 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
public class SendgridMailerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SendgridMailerApplication.class, args);
    }

}
