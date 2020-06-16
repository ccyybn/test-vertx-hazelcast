package com.xxx.topicagent;

import com.xxx.util.LogHelper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StartVerticle {
    @Resource
    private Vertx vertx;
    //    @Resource
//    private Agent agent;
    @Resource
    private Topic topic;
    @Value("#{environment.MODE}")
    private String MODE;

    @Value("#{environment.AGENT_COUNT}")
    private Integer agentCount = 1;

    @PostConstruct
    public void init() {
        if ("topic".equals(MODE)) {
            vertx.deployVerticle(topic, r -> LogHelper.deploying("Topic", r, log));
        }
        if ("agent".equals(MODE)) {
            List<Agent> agents = new ArrayList<>();
            for (int i = 0; i < agentCount; i++) {
                Agent agent = EntryPoint.getBean(Agent.class, i);
                agents.add(agent);
                vertx.deployVerticle(agent, r -> LogHelper.deploying("Agent", r, log));
            }
//            vertx.setPeriodic(1000, l -> {
//                List<Long> times = agents.stream().map(a -> a.times).flatMap(Collection::stream).collect(Collectors.toList());
//                Helper.print("TIMES", times);
//            });
        }
    }
}
