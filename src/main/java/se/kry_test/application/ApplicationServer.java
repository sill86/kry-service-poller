package se.kry_test.application;

import se.kry_test.database.ServiceDAOImpl;
import se.kry_test.service.ServicePoller;
import se.kry_test.database.ServiceDAO;
import se.kry_test.model.ServiceEnum;

import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import java.util.UUID;

public class ApplicationServer extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(ApplicationServer.class);
  private ServicePoller servicePoller;
  private ServiceDAO serviceDAO;
  private RouteHandler routeHandler;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    serviceDAO = new ServiceDAOImpl(vertx);

    serviceDAO.createTableIfNotExist().setHandler(createTableHandler -> {
      if(createTableHandler.succeeded()){
        logger.info("Table service created");
      } else {
        logger.info("Error during the creation of service table", createTableHandler.cause());
      }
    });
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    HttpServer server = vertx.createHttpServer();
    routeHandler = new RouteHandler(serviceDAO);
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.route().handler(StaticHandler.create());
    setCookie(router);
    createRouter(router);

    servicePoller = new ServicePoller(vertx, serviceDAO);
    vertx.setPeriodic(60000, timerId -> servicePoller.poll());

    server.requestHandler(router).listen(8080, http -> {
      if (http.succeeded()) {
        logger.info("HTTP server started on port 8080");
        startPromise.complete();
      } else {
        logger.info("HTTP server failed to start");
        startPromise.fail(http.cause());
      }
    });

  }

  private void setCookie(Router router) {
    router.route().handler(CookieHandler.create());
    router.route().handler(ctx -> {
      Cookie clientCookieId = ctx.getCookie(ServiceEnum.COOKIE.name());
      String cookieValue;
      if (clientCookieId == null) {
        cookieValue = UUID.randomUUID().toString();
      } else {
        cookieValue = clientCookieId.getValue();
      }
      ctx.addCookie(Cookie.cookie(ServiceEnum.COOKIE.name(), cookieValue));
      ctx.next();
    });

  }

  private void createRouter(Router router){
    router.route("/*").handler(StaticHandler.create());
    router.route("/*").handler(
      CorsHandler.create(".*.")
        .allowedMethod(io.vertx.core.http.HttpMethod.GET)
        .allowedMethod(io.vertx.core.http.HttpMethod.POST)
        .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
        .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
        .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
    );
    router.get("/service").handler(routeHandler::getServicesByUser);
    router.post("/service").handler(routeHandler::addService);
    router.put("/service}").handler(routeHandler::updateService);
    router.delete("/service").handler(routeHandler::deleteService);
  }

}
