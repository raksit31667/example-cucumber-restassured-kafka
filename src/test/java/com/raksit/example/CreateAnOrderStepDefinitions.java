package com.raksit.example;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
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

  private String accessToken;
  private String orderRequestBody;
  private String createdOrderId;
  private List<String> records;

  @Given("a user has valid access token")
  public void getAccessToken() {
    Response response = given()
        .config(RestAssuredConfig.config()
            .encoderConfig(EncoderConfig.encoderConfig()
                .encodeContentTypeAs("x-www-form-urlencoded", ContentType.URLENC)))
        .contentType(ContentType.URLENC)
        .formParam("client_id", System.getProperty("service.client.id"))
        .formParam("client_secret", System.getProperty("service.client.secret"))
        .formParam("grant_type", "client_credentials")
        .formParam("resource", System.getProperty("service.resource"))
        .when()
        .post(String.format("https://login.microsoftonline.com/%s/oauth2/token", System.getProperty("service.tenant.id")))
        .then()
        .statusCode(HttpStatus.SC_OK)
        .extract()
        .response();

    accessToken = response.jsonPath().getString("access_token");
  }

  @Given("an order request as described in {string}")
  public void readAnOrderRequestFromJsonFile(String jsonPath) throws IOException {
    File file = FileUtils.getFile("src", "test", "resources", jsonPath);
    orderRequestBody = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
  }

  @When("a user sends the request to create an order successfully")
  public void createAndOrder() {
    Response response = given()
        .contentType(ContentType.JSON)
        .body(orderRequestBody)
        .when()
        .header("Authorization",
            "Bearer " + accessToken)
        .post(System.getProperty("order.service.url") + "/orders")
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
