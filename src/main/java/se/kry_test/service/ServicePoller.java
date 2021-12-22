package se.kry_test.service;

import se.kry_test.application.ServiceEnum;
import se.kry_test.database.ServiceDAO;
import se.kry_test.database.Service;

import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.logging.Logger;
import io.vertx.core.Vertx;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;

public class ServicePoller {

  private static final Logger logger = LoggerFactory.getLogger(ServicePoller.class);
  private final WebClient webClient;
  private final ServiceDAO serviceDAO;

  public ServicePoller(Vertx vertx, ServiceDAO serviceDAO) {
    this.webClient = WebClient.create(vertx, new WebClientOptions().setConnectTimeout(5000));
    this.serviceDAO = serviceDAO;
  }

  public void poll() {
    this.serviceDAO.getServices().setHandler(getHandler -> {
      if (getHandler.succeeded()) {
        List<Service> serviceList = getHandler.result();
        serviceList.stream().forEach(service -> {
          this.webClient.getAbs(service.getUrl()).send(result -> {
            if (result != null || result.result() != null) {
              String newStatus = ServiceEnum.STATUS_UNKNOWN.label();
              if (result.result().statusCode() == HttpResponseStatus.OK.code()) {
                newStatus = ServiceEnum.STATUS_OK.label();
              } else {
                newStatus = ServiceEnum.STATUS_FAIL.label();
              }
              if (!service.getStatus().equals(newStatus)) {
                service.setStatus(newStatus);
                this.serviceDAO.updateStatus(service).setHandler(updHandler -> {
                  if (updHandler.succeeded()) {
                    logger.info("updated url: " + service.getUrl() + " with status " + service.getStatus());
                  } else {
                    logger.error("failure updating url: " + service.getUrl() + " with status " + service.getStatus() + " due to " + updHandler.cause().getMessage());
                  }
                });
              }
            }
          });
        });
      } else {
        logger.error("failure fetching services due to " + getHandler.cause().getMessage());
      }
    });
  }

}
