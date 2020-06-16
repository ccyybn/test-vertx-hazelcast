package com.xxx.websocket;

import com.xxx.util.Helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerStart extends AbstractVerticle {
    private static int count = 0;
    private static Vertx vertx;
    private static String msg = "{\"topic\":\"/baccarat/B3\",\"push\":{\"timestamp\":1496886182390,\"version\":17299,\"addition\":[{\"$\":[\"o.set\",\"cards\",{}]},{\"$\":[\"o.set\",\"cards_order\",[]]},{\"$\":[\"o.set\",\"score\",{\"banker\":0.0,\"player\":0.0,\"tie\":false,\"banker_pair\":false,\"player_pair\":false}]},{\"$\":[\"o.set\",\"stage\",\"shuffle\"]},{\"$\":[\"o.set\",\"bet_statistics\",{\"banker_pair\":{\"users\":\"0\",\"amount\":0.0},\"player_pair\":{\"users\":\"0\",\"amount\":0.0},\"player\":{\"users\":\"0\",\"amount\":0.0},\"banker\":{\"users\":\"0\",\"amount\":0.0},\"tie\":{\"users\":\"0\",\"amount\":0.0}}]},{\"$\":[\"o.set\",\"payout\",{\"1\":{},\"2\":{},\"3\":{},\"5\":{},\"6\":{},\"7\":{},\"8\":{}}]},{\"$\":[\"o.set\",\"bet\",{\"1\":{},\"2\":{},\"3\":{},\"5\":{},\"6\":{},\"7\":{},\"8\":{}}]},{\"$\":[\"o.set\",\"natural_winner\",false]},{\"$\":[\"o.set\",\"round_statistics\",{\"rounds\":0.0,\"banker\":0.0,\"player\":0.0,\"tie\":0.0}]}]}}";
    private static List<ServerStart> agents = new ArrayList<>();
    static final int CLIENT_COUNT = 2000;

    private static String START_NOTIFY = "/start_notify";
    private static AtomicInteger counterEnd = new AtomicInteger(0);
    private static AtomicInteger counterStart = new AtomicInteger(0);
    private static AtomicInteger counterConnect = new AtomicInteger(0);

    public static void main(String[] args) {
        VertxOptions options = new VertxOptions();
        options.setEventLoopPoolSize(10);
        vertx = Vertx.vertx(options);
        log.debug("Message to send length[{}]", msg.length());
        HttpServer httpServer = vertx.createHttpServer();
        httpServer.websocketHandler(ServerStart::onConnection);
        httpServer.listen(12345);
    }

    private SocketBridge bridge;
    private int number;
    private List<Long> times = new ArrayList<>();

    private ServerStart(Vertx vertx, ServerWebSocket socket, int number) {
        this.bridge = new SocketBridge(vertx, socket);
        this.number = number;
    }

    private static long startTime;

    private static void onConnection(ServerWebSocket socket) {
        ServerStart agent = new ServerStart(vertx, socket, ++count);
        agents.add(agent);
        vertx.deployVerticle(agent, r -> {
            if (r.failed()) log.error("", r.cause());
            else {
                log.debug("onConnection[{}]", counterConnect.incrementAndGet());
                if (counterConnect.get() == CLIENT_COUNT) {
                    startTime = System.currentTimeMillis();
                    vertx.eventBus().publish(START_NOTIFY, "");
                }
            }
        });
    }

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(START_NOTIFY, this::onStart);
    }

    private void onStart(Message<String> message) {
        log.debug("[ID:{}]start push[COUNT:{}]", number, counterStart.incrementAndGet());
        for (int i = 0; i < CLIENT_COUNT; i++) {
            long start = System.nanoTime();
            bridge.write(msg);
            times.add(System.nanoTime() - start);
        }
        log.debug("[ID:{}]end push[COUNT:{}]", number, counterEnd.incrementAndGet());
        if (counterEnd.get() == CLIENT_COUNT) {
            List<Long> collect = agents.stream().map(a -> a.times).flatMap(Collection::stream).collect(Collectors.toList());
            Helper.print("Statistics", collect);
            log.debug("[Duration]:{} ms", System.currentTimeMillis() - startTime);
        }
    }
}
