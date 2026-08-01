package com.ronext.optimizer.adapter.in.http;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/** Cloud Run entry point. SERVICE_MODE selects the public API or private optimization worker. */
public final class OptimizationHttpServer {
    private OptimizationHttpServer() {
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        switch (System.getenv().getOrDefault("SERVICE_MODE", "api")) {
            case "api" -> new OptimizationApiController().register(server);
            case "worker" -> new OptimizationWorkerController().register(server);
            default -> throw new IllegalArgumentException("SERVICE_MODE must be api or worker");
        }
        server.start();
    }
}
