package com.xxx.util;

import org.slf4j.Logger;

import io.vertx.core.AsyncResult;

public class LogHelper {
    private static <T> void logForComponent(String name, AsyncResult<T> result, Logger logger,
                                            String action, String participle) {
        if (result.succeeded()) {
            logger.info("{} {}", name, participle);
        } else {
            logger.warn("Failed to " + action + " " + name, result.cause());
        }
    }

    public static <T> void starting(String name, AsyncResult<T> result, Logger logger) {
        logForComponent(name, result, logger, "start", "started");
    }

    public static <T> void deploying(String name, AsyncResult<T> result, Logger logger) {
        logForComponent(name, result, logger, "deploy", "deployed");
    }

    public static <T> void stopping(String name, AsyncResult<T> result, Logger logger) {
        logForComponent(name, result, logger, "stop", "stopped");
    }

    public static <T> void undeploying(String name, AsyncResult<T> result, Logger logger) {
        logForComponent(name, result, logger, "undeploy", "undeployed");
    }
}
