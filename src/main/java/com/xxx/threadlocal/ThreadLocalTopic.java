package com.xxx.threadlocal;

import com.google.gson.Gson;

import com.xxx.util.LogHelper;

import java.util.HashMap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadLocalTopic extends AbstractVerticle {
    private static ThreadLocal<HashMap<String, String>> threadLocal = ThreadLocal.withInitial(HashMap::new);
//    private static ThreadLocal<String> threadLocal= new ThreadLocal<>();

    private int number;

    public ThreadLocalTopic(int number) {
        this.number = number;
    }

    @Override
    public void start() throws Exception {
//        threadLocal = ThreadLocal.withInitial(() -> new HashMap<>());
        vertx.setPeriodic(1000, l -> {
            HashMap<String, String> map = threadLocal.get();
            log.debug("[{}] local [{}]", number, new Gson().toJson(map));
            if (number == 0) {
                log.debug("set value at map");
                map.put("1", "1");
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        for (int i = 0; i < 10; i++) {
            ThreadLocalTopic localTopic = new ThreadLocalTopic(i);
            vertx.deployVerticle(localTopic, r -> LogHelper.deploying("Local", r, log));
        }
    }
}
