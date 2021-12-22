package se.kry_test.java.se.kry_test.application;

import se.kry_test.application.ApplicationServer;

import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.Vertx;

import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

@ExtendWith(VertxExtension.class)
public class TestApplicationServer {
  private Vertx vertx;
  private WebClient webClient;
  private static final String URL = "http://html.it";
  private static final String NAME = "http://html.it";

  @BeforeEach
  void setup(VertxTestContext testContext) {
    this.vertx = Vertx.vertx();
    this.webClient = WebClient.create(vertx);

    vertx.deployVerticle(ApplicationServer.class.getName(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @AfterEach
  public void finish(VertxTestContext testContext) {
    vertx.close(testContext.succeeding(response -> {
      testContext.completeNow();
    }));
  }

  @Test
  @DisplayName("Fetch Services on localhost:8080/service")
  @Timeout(value = 10)
  void getRequestWebserver(VertxTestContext testContext) {
    webClient
      .get(8080, "localhost", "/service")
      .send(response -> testContext.verify(() -> {
        assertEquals(200, response.result().statusCode());
        testContext.completeNow();
      }));
  }

  @Test
  @DisplayName("Post a service on localhost:8080/service")
  @Timeout(value = 10)
  void postRequestWebserver(VertxTestContext testContext) {
    JsonObject serviceJson = new JsonObject()
      .put("url", URL)
      .put("name", NAME);

    webClient.post(8080,"localhost", "/service")
      .sendJsonObject(serviceJson, ar -> {
        assertEquals(true, ar.succeeded());
        testContext.completeNow();
    });
  }

  @Test
  @DisplayName("Delete a service on localhost:8080/service")
  @Timeout(value = 10)
  void deletRequestWebserver(VertxTestContext testContext) {
    JsonObject serviceJson = new JsonObject()
      .put("url", URL);

    webClient.delete(8080,"localhost", "/service")
      .sendJsonObject(serviceJson, ar -> {
        assertEquals(true, ar.succeeded());
        testContext.completeNow();
      });
  }

}


