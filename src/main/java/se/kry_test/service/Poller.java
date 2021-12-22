package se.kry_test.service;

import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class Poller {
    public static final int DEFAULT_TIMEOUT_MS = 3000;
    private final WebClient webClient;

    public Poller(Vertx vertx) {
        this.webClient = WebClient.create(vertx, new WebClientOptions().setConnectTimeout(5000));
    }

    public Future<Boolean> pollService(String url) {
        Future<Boolean> future = Future.future();
        webClient.getAbs(url).timeout(DEFAULT_TIMEOUT_MS).send(req -> future.complete(req.succeeded()));
        return future;
    }
}
