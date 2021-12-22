package se.kry_test.application;

import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Promise;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class Config {
  public static Future<JsonObject> createConfig(final Vertx vertx) {

    final Promise<JsonObject> promise = Promise.promise();
    final ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(createOptions()));
    retriever.getConfig(handler -> {
      if (handler.failed()) {
        promise.fail(handler.cause());
        return;
      }
      promise.complete(handler.result());
    });
    return promise.future();
  }

  private static ConfigStoreOptions createOptions() {
    //TODO: move configuration inside resources folder in a json file
    return new ConfigStoreOptions()
      .setType("json")
      .setConfig(new JsonObject()
        .put("url", "jdbc:mysql://localhost:3309/dev?allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true&useSSL=false")
        .put("user", "dev")
        .put("password", "secret")
        .put("driver_class", "com.mysql.jdbc.Driver")
        .put("http_port", "8080"));
  }
}
