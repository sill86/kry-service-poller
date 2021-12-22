package se.kry_test.application;

import se.kry_test.database.Service;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import se.kry_test.database.ServiceDAO;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.Json;

import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

public class RouteHandler {
  private static final String PATTERN = "^(https?|http?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
  private static final Logger logger = LoggerFactory.getLogger(RouteHandler.class);
  private ServiceDAO serviceDAO;
  private Pattern pattern;

  public RouteHandler(ServiceDAO serviceDAO) {
    this.serviceDAO = serviceDAO;
    this.pattern = Pattern.compile(PATTERN);
  }

  public void getServicesByUser(RoutingContext context) {
    final Cookie kryClientCookie = context.getCookie(ServiceEnum.COOKIE.name());
    this.serviceDAO.getServicesByUser(kryClientCookie.getValue()).setHandler(getHandler -> {
      if (getHandler.succeeded()) {
        List<JsonObject> jsonServices = getHandler.result().stream().map(Service::toJson).collect(Collectors.toList());
        this.putHeaderDefault(context.response());
        context.response()
          .setStatusCode(HttpResponseStatus.OK.code())
          .end(new JsonArray(jsonServices).encode());
      } else {
        context.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
          .putHeader("content-type", "text/plain")
          .end(getHandler.cause().getMessage());
        logger.error("Could not fetch services", getHandler.cause());
      }
    });
  }

  public void addService(RoutingContext context) {
    Service service = prepareServiceForDBOperation(context);

    Matcher matcher = pattern.matcher(service.getUrl());
    if (!matcher.matches()) {
      context.response()
        .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
        .putHeader("content-type", "text/plain")
        .setStatusMessage("Bad URL formatting")
        .end("The URL is not valid");
      return;
    }

    if (service.getName().isEmpty() || service.getUrl().isEmpty()) {
      context.response()
        .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
        .putHeader("content-type", "text/plain")
        .end("{'error': 'Name and URL must by non-empty'}");
      return;
    }

    this.serviceDAO.add(service).setHandler(addHandler -> {
      this.putHeaderDefault(context.response());
      if (addHandler.failed()) {
        context.fail(addHandler.cause());
        return;
      }
      context.response()
        .setStatusCode(HttpResponseStatus.OK.code())
        .end(HttpResponseStatus.OK.codeClass().name());
    });
  }

  public void updateService(RoutingContext context) {
    try {
      Service service = prepareServiceForDBOperation(context);

      this.serviceDAO.update(service).setHandler(updHandler -> {
        this.putHeaderDefault(context.response());
        if (updHandler.failed()) {
          context.fail(updHandler.cause());
          return;
        }
        context.response()
          .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
          .end();
      });
    } catch (final Exception e) {
      context.fail(e);
    }
  }

  public void deleteService(RoutingContext context) {
    try {
      Service service = prepareServiceForDBOperation(context);

      this.serviceDAO.delete(service).setHandler(deleteHandler -> {
        this.putHeaderDefault(context.response());
        if (deleteHandler.failed()) {
          context.fail(deleteHandler.cause());
          return;
        }
        context.response()
          .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
          .end();
      });
    } catch (final Exception e) {
      context.fail(e);
    }
  }

  private void putHeaderDefault(final HttpServerResponse response) {
    response.putHeader("content-type", "application/json");
  }

  private Service prepareServiceForDBOperation(RoutingContext context) {
    Service service = Json.decodeValue(context.getBodyAsString(), Service.class);
    final Cookie kryClientCookie = context.getCookie(ServiceEnum.COOKIE.name());
    service.setUserCookie(kryClientCookie.getValue());
    return service;
  }
}
