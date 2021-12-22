package se.kry_test;

import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.Promise;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import se.kry_test.application.ApplicationServer;
import se.kry_test.application.Config;

public class Start {

  private static final Logger logger = LoggerFactory.getLogger(Start.class);
  private static final String APPLICATION_SUCCESS_INIT = "Application Started";
  private static final String ERROR = "Init Error";

  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx();
    Config.createConfig(vertx)
      .compose(config -> {
        return createApplicationServer(vertx, config);
      })
      .setHandler(res -> {
        if (res.failed()) {
          logger.error(ERROR);
          return;
        }
        logger.info(APPLICATION_SUCCESS_INIT);
      });

  }

  private static Future<Void> createApplicationServer(final Vertx vertx, final JsonObject config) {
    final Promise<Void> promise = Promise.promise();
    final DeploymentOptions options = new DeploymentOptions().setConfig(config);

    vertx.deployVerticle(new ApplicationServer(), options, event -> {
      if (event.failed()) {
        promise.fail(event.cause());
        return;
      }
      promise.complete();
    });
    return promise.future();
  }

}
