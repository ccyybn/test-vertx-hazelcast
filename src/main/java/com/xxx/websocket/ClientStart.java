package com.xxx.websocket;

import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientStart extends AbstractVerticle {
    public static Vertx vertx;

    private static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) {
        vertx = Vertx.vertx();
        for (int i = 0; i < ServerStart.CLIENT_COUNT; i++) {
            vertx.deployVerticle(new ClientStart());
        }
    }

    @Override
    public void start() throws Exception {
        HttpClient httpClient = vertx.createHttpClient();
        httpClient.websocket(12345, "192.168.8.164", "", socket -> {
            log.debug("Connected[{}]", counter.incrementAndGet());
        });
    }
}
