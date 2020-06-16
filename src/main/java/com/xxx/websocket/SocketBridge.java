package com.xxx.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Since ServerWebSocket inherits context from its listener, all the handlers attached to it
 * have to run in the same thread. Therefore, SocketBridge is intended to exchange buffers
 * between socket and event bus to make data handlers running in other threads possible.
 * </p>
 */
@Slf4j
public class SocketBridge {
    private final EventBus bus;
    private final ServerWebSocket socket;
    private final String id;
    private final String inbound;
    private final String outbound;
    private MessageConsumer<Buffer> ic;
    private MessageConsumer<String> oc;
    private List<Buffer> pending = new ArrayList<>();
    private boolean closed = false;

    /**
     * Create socket bridge.
     * It should be invoked in connection handlers by socket listener. That means the constructor
     * is supposed to run in the same thread of listener.
     * This constructor setup an event bus consumer named outbound for writing data to socket and
     * create a buffer for inbound data which should be flushed after the real verticle to handle
     * socket data deployed. This buffer is necessary because verticle deploying is asynchronized,
     * unless data loss is required between the duration of verticle deploying.
     *
     * @param vertx  global vertx
     * @param socket the incoming web socket
     */
    public List<Long> writeTimes = new ArrayList<>();
    public List<Long> sendTimes = new ArrayList<>();
    public List<Long> receiveTimes = new ArrayList<>();

    public SocketBridge(Vertx vertx, ServerWebSocket socket) {
        bus = vertx.eventBus();
        this.socket = socket;
        id = UUID.randomUUID().toString();
        inbound = "BRIDGE.inbound." + id;
        outbound = "BRIDGE.outbound." + id;
        oc = bus.consumer(outbound, msg -> {
            long start = System.nanoTime();
            String result = msg.body();
            if (result != null) {
                socket.writeFinalTextFrame(result);
            } else {
                socket.close();
            }
            writeTimes.add(System.nanoTime() - start);
        });
        socket.handler(b -> pending.add(b));
        socket.closeHandler(v -> closed = true);
    }

    /**
     * Setup real handlers.
     * It should be invoked in deployed verticles' start(). The thread might not be as same as
     * the one of listener.
     *
     * @param handler replacement with ServerWebSocket::handler
     * @param close   replacement with ServerWebSocket::closeHandler
     */
    public void setupHandlers(Handler<Buffer> handler, Runnable close) {
        ic = bus.consumer(inbound, msg -> {
            Buffer data = msg.body();
            if (data != null) {
                handler.handle(data);
                return;
            }
            ic.unregister();
            close.run();
        });
    }

    /**
     * Write data to socket.
     * It should be invoked in deployed verticles' handlers.
     *
     * @param result data to be written.
     */
    public void write(String result) {
        long start = System.nanoTime();
        bus.send(outbound, result);
        sendTimes.add(System.nanoTime() - start);
    }

    /**
     * Flush buffered data to deployed verticles.
     * It should be invoked by listener.
     */
    public void flush() {
        pending.forEach(buffer -> bus.send(inbound, buffer));
        pending.clear();
        socket.handler(b -> bus.send(inbound, b));
        if (closed) onClose();
        else socket.closeHandler(v -> onClose());
    }

    private void onClose() {
        oc.unregister();
        bus.send(inbound, null);
    }

    public void close() {
        bus.send(outbound, null);
    }
}
