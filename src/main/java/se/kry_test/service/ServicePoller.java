package se.kry_test.service;

import se.kry_test.database.ServiceDAO;
import se.kry_test.model.ServiceStatus;
import se.kry_test.model.Service;

import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.Logger;
import io.vertx.core.Vertx;

import java.util.List;

public class ServicePoller {

  private static final Logger logger = LoggerFactory.getLogger(ServicePoller.class);
  private final ServiceDAO serviceDAO;
  private final Poller poller;

  public ServicePoller(Vertx vertx, ServiceDAO serviceDAO) {
    this.poller = new Poller(vertx);
    this.serviceDAO = serviceDAO;
  }

  public void poll() {
    this.serviceDAO.getServices().setHandler(getHandler -> {
      if (getHandler.succeeded()) {
        List<Service> serviceList = getHandler.result();
        serviceList.stream().forEach(service -> {
          poller.pollService(service.getUrl()).setHandler(pollReq-> {
            if (pollReq != null || pollReq.result() != null) {
              ServiceStatus newStatus = Boolean.TRUE.equals(pollReq.result()) ? ServiceStatus.OK : ServiceStatus.FAIL;
              if (!service.getStatus().equals(newStatus.label())) {
                service.setStatus(newStatus.label());
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
