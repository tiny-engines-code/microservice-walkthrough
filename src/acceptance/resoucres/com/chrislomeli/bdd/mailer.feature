Feature: Is it Friday yet?
  Everybody wants to know when it's Friday

  Scenario: Sunday isn't Friday
    Given today is Sunday
    When I ask whether it's Friday yet
    Then I should be told Nope

  Scenario: Correct non-zero number of books found by author
    Given I have the following books in the store by map
      | title                                | author      |
      | The Devil in the White City          | Erik Larson |
      | The Lion, the Witch and the Wardrobe | C.S. Lewis  |
      | In the Garden of Beasts              | Erik Larson |
    When I search for books
    Then I find all books



    Scenario: Blah
      Given API is unavailable
      Then print availablity


  Scenario: Blah2
    Given API is returning 412
    Then print API returns status