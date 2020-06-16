package com.xxx.topicagent;

import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Topic extends AbstractVerticle {
    public static final String ADDRESS = "/topic";
    public static final String ADDRESS_PUSH = "/topic_push";

    private MessageProducer<Object> publisher;
    private MessageConsumer<String> consumer;

    @Override
    public void start() throws Exception {
        consumer = vertx.eventBus().consumer(ADDRESS, this::onMessage);
        publisher = vertx.eventBus().publisher(ADDRESS_PUSH);
//        vertx.setPeriodic(1000, l -> publisher.write("12345678901234567890"));
    }

    public void onMessage(Message<String> message) {
        byte[] bytes = new byte[1000];
        log.debug("topic receive [{}]", message.body());
        message.reply("Reply to:" + message.body());

//        long start = System.currentTimeMillis();
//        Date date = new Date();
//        for (int i = 0; i < 1000; i++) {
//            Gson gson = new Gson();
//            String s = gson.toJson(date);
//        }
//        log.debug("calculate time[{}]", System.currentTimeMillis() - start);
        publisher.write("Push of:" + message.body());
    }

    @Override
    public void stop() throws Exception {
        consumer.unregister();
        publisher.close();
    }
}
