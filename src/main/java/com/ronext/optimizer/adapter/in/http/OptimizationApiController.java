package com.ronext.optimizer.adapter.in.http;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.workflows.executions.v1.CreateExecutionRequest;
import com.google.cloud.workflows.executions.v1.Execution;
import com.google.cloud.workflows.executions.v1.ExecutionsClient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

final class OptimizationApiController {
    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private final ExecutionsClient executions = ExecutionsClient.create();

    OptimizationApiController() throws IOException {
    }

    void register(HttpServer server) {
        server.createContext("/optimizations", this::handleOptimizations);
    }

    private void handleOptimizations(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if ("POST".equals(exchange.getRequestMethod()) && "/optimizations".equals(path)) {
                submit(exchange);
                return;
            }
            if ("GET".equals(exchange.getRequestMethod()) && path.startsWith("/optimizations/")) {
                getResult(exchange, path.substring("/optimizations/".length()));
                return;
            }
            HttpJson.error(exchange, 404, "Not found");
        } catch (IllegalArgumentException exception) {
            HttpJson.error(exchange, 400, exception.getMessage());
        } catch (Exception exception) {
            HttpJson.error(exchange, 500, "Unable to process optimization request");
        }
    }

    private void submit(HttpExchange exchange) throws Exception {
        Map<String, Object> body = JsonSupport.MAPPER.readValue(exchange.getRequestBody(), Map.class);
        String inputUri = requiredString(body, "inputUri");
        if (!inputUri.startsWith("gs://")) {
            throw new IllegalArgumentException("inputUri must be a gs:// URI");
        }

        String requestId = UUID.randomUUID().toString();
        Map<String, Object> parameters = map(body.get("parameters"));
        parameters.put("parallelRuns", bounded(parameters.get("parallelRuns"), 8, 1, 20));
        parameters.put("iterationsPerRun", bounded(parameters.get("iterationsPerRun"), 5_000, 100, 250_000));
        parameters.put("seed", longNumber(parameters.get("seed"), System.nanoTime()));

        Map<String, Object> workflowInput = new LinkedHashMap<>();
        workflowInput.put("requestId", requestId);
        workflowInput.put("inputUri", inputUri);
        workflowInput.put("parameters", parameters);
        workflowInput.put("submittedAt", Instant.now().toString());

        executions.createExecution(CreateExecutionRequest.newBuilder()
                .setParent(requiredEnvironment("WORKFLOW_NAME"))
                .setExecution(Execution.newBuilder().setArgument(JsonSupport.write(workflowInput)).build())
                .build());

        HttpJson.respond(exchange, 202, Map.of(
                "requestId", requestId,
                "status", "ACCEPTED",
                "statusPath", "/optimizations/" + requestId));
    }

    private void getResult(HttpExchange exchange, String requestId) throws IOException {
        if (requestId.isBlank() || requestId.contains("/")) {
            throw new IllegalArgumentException("requestId is required");
        }
        var blob = storage.get(BlobId.of(requiredEnvironment("RESULTS_BUCKET"), "results/" + requestId + ".json"));
        if (blob == null) {
            HttpJson.respond(exchange, 202, Map.of("requestId", requestId, "status", "RUNNING"));
            return;
        }
        exchange.getResponseHeaders().set("content-type", "application/json; charset=utf-8");
        byte[] bytes = blob.getContent();
        exchange.sendResponseHeaders(200, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> input ? new LinkedHashMap<>((Map<String, Object>) input) : new LinkedHashMap<>();
    }

    private static int bounded(Object value, int fallback, int minimum, int maximum) {
        int number = value instanceof Number input ? input.intValue() : fallback;
        return Math.clamp(number, minimum, maximum);
    }

    private static long longNumber(Object value, long fallback) {
        return value instanceof Number input ? input.longValue() : fallback;
    }

    private static String requiredString(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return text;
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is not configured");
        }
        return value;
    }
}
