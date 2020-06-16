Feature: Create an Order
  Scenario: Happy path
    Given a user has valid access token
    And an order request as described in "order.json"
    When a user sends the request to create an order successfully
    And wait for notification from the system within 5 seconds
    Then a user should receive a notification with a correct id

