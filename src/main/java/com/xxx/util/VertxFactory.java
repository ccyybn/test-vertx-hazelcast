package com.xxx.util;

import com.google.common.collect.ImmutableList;

import com.hazelcast.config.Config;
import com.hazelcast.instance.GroupProperty;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

@Component
public class VertxFactory {
    private Vertx vertx;
    @Value("#{environment.HOSTNAME}")
    private String HOSTNAME;
    @Value("#{environment.BUS_IN_PORT}")
    private int BUS_IN_PORT;
    @Value("#{environment.BUS_OUT_PORT}")
    private int BUS_OUT_PORT;
    @Value("#{environment.CLUSTER_IN_PORT}")
    private int CLUSTER_IN_PORT;
    @Value("#{environment.CLUSTER_OUT_PORT}")
    private int CLUSTER_OUT_PORT;
    @Value("#{environment.HOST}")
    private String HOST;
    @Value("#{environment.JOIN}")
    private String JOIN;

    @PostConstruct
    public void init() throws ExecutionException, InterruptedException {

        Config hazelcastConfig = new Config();
        hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig()
                .setMembers(ImmutableList.of(JOIN))
                .setEnabled(true);
        hazelcastConfig.getNetworkConfig().setPort(CLUSTER_IN_PORT).getJoin().getMulticastConfig().setEnabled(false);
        hazelcastConfig.getNetworkConfig().setPublicAddress(HOST + ":" + CLUSTER_OUT_PORT);
        hazelcastConfig.setProperty(GroupProperty.SHUTDOWNHOOK_ENABLED, "false");

        ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        options.setClusterHost(HOSTNAME).setClusterPort(BUS_IN_PORT)
                .setClusterPublicHost(HOST)
                .setClusterPublicPort(BUS_OUT_PORT);

        CompletableFuture<Vertx> future = new CompletableFuture<>();
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                future.complete(vertx);
            } else {
                future.completeExceptionally(res.cause());
            }
        });
        vertx = future.get();
    }

    public Vertx createVertx() {
        return vertx;
    }
}
