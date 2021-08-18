Feature: Sendgrid acceptance test example
  A few example bdd tests using cucumber as the driver

  Scenario: Invalid values should be caught before sending to the API
    Given the API is available
    When I am sending valid and invalid emails
      | sender | from           | to              | subject     | args             | body                     | expected |
      | "me"   | me@gmail.com   | you@gmail.com   | Hello world | {"head": "bang"} | I was a teenage werewolf | 200      |
      | "john" | me@gmail.com   | ERROR.gmail.com | Hello world | {"head": "bang"} | I was a teenage werewolf | 400      |
      | "fred" | ERROR@gmailcom | you@gmail.com   | Hello world | {"head": "bang"} | I was a teenage werewolf | 400      |
      | "me"   | me@gmail.com   | you@gmail.com   |             | {"head": "bang"} | I was a teenage werewolf | 400      |
      | "me"   | me@gmail.com   | you@gmail.com   | Hello world |                  | I was a teenage werewolf | 200      |
      | "me"   | me@gmail.com   | you@gmail.com   | Hello world | {"head": "bang"} |                          | 200      |
      |        | me@gmail.com   | you@gmail.com   | Hello world | {"head": "bang"} | I was a teenage werewolf | 200      |
    Then Validation errors should be caught before being sent to the API

  Scenario: The solution should handle anything Sendgrid can throw at us
    Given I am sending any well-formated email
    When The API returns any code
      | 200-202 |
      | 428     |
      | 429     |
      | 431     |
      | 400-426 |
      | 500-511 |
    Then I should receive the same code
