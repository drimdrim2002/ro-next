package com.ronext.optimizer.adapter.in.http;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

final class HttpJson {
    private HttpJson() {
    }

    static void respond(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = JsonSupport.write(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("content-type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    static void error(HttpExchange exchange, int statusCode, String message) throws IOException {
        respond(exchange, statusCode, Map.of("message", message));
    }
}
