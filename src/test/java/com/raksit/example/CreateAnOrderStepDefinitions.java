package com.raksit.example;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CreateAnOrderStepDefinitions {

  private String orderRequestBody;
  private String createdOrderId;
  private List<String> records;

  @Given("a order request as described in {string}")
  public void readAnOrderRequestFromJsonFile(String jsonPath) throws IOException {
    File file = FileUtils.getFile("src", "test", "resources", jsonPath);
    orderRequestBody = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
  }

  @When("send a request to create an order successfully")
  public void createAndOrder() {
    Response response = given()
        .contentType(ContentType.JSON)
        .body(orderRequestBody)
        .when()
        .header("Authorization",
            "Bearer " + System.getProperty("token"))
        .post(System.getProperty("orderHostName") + "/orders")
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .extract()
        .response();

    createdOrderId = response.jsonPath().get("id");
  }

  @When("wait for notification from the system within {int} seconds")
  public void waitForSystemNotification(int waitingSeconds) throws IOException {
    records = new NotificationReceiver("order.created").poll(waitingSeconds);
  }

  @Then("a user should receive a notification with a correct id")
  public void shouldReceivedNotificationWithCorrectId() {
    assertThat(records.stream().anyMatch(record -> record.contains(createdOrderId)), equalTo(true));
  }
}
