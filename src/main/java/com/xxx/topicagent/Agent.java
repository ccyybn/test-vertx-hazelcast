package com.xxx.topicagent;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class Agent extends AbstractVerticle {
    public String id = UUID.randomUUID().toString();
    private long seq = 0;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private long agentNumber = 0;

    public Agent(long agentNumber) {
        this.agentNumber = agentNumber;
    }

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(Topic.ADDRESS_PUSH, this::onPush);
        final long[] start = {System.currentTimeMillis()};
        vertx.setPeriodic(1000, rr -> {
            Future<Message<String>> reply = Future.future();
            byte[] bytes = new byte[1000];
            String msg = "[" + id + "]Request[" + ++seq + "]" + new String(bytes);
            if (agentNumber == 0)
                log.debug("Start request at time[{}] interval {} ms", sdf.format(new Date()), System.currentTimeMillis() - start[0]);
            start[0] = System.currentTimeMillis();
            vertx.eventBus().send(Topic.ADDRESS, msg, reply.completer());
            reply.setHandler(r -> {
                if (r.failed()) log.error("", r.cause());
                else onReply(r.result());
            });
        });

    }

    private long messageCount = 0L;

    public void onPush(Message<String> message) {
        messageCount++;
        doCalculate();
//        log.debug("[{}] on push [{}]", id, message.body());

        if (agentNumber == 0 && messageCount == 1000) {
            log.debug("[{}] on push finish", id);
            messageCount = 0;
        }
    }

    public List<Long> times = new ArrayList<>();

    public void doCalculate() {
        long start = System.nanoTime();
        double a = 0;
        for (int i = 0; i < 5000; i++) {
            a++;
        }
        times.add(System.nanoTime() - start);
    }

    public void onReply(Message<String> message) {
//        log.debug("[{}]on reply[{}]", id, message.body());
    }
}
