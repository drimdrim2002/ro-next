package com.ronext.optimizer.adapter.in.http;

import com.google.cloud.storage.StorageOptions;
import com.google.cloud.workflows.executions.v1.ExecutionsClient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

final class OptimizationApiController {
    private final LegacyStorage storage;
    private final LegacyWorkflowExecutor executions;
    private final Function<String, String> environment;
    private final Supplier<String> requestIds;
    private final LongSupplier defaultSeeds;
    private final Supplier<Instant> clock;

    OptimizationApiController() throws IOException {
        this(
                new GcpLegacyStorage(StorageOptions.getDefaultInstance().getService()),
                new GcpLegacyWorkflowExecutor(ExecutionsClient.create()),
                System::getenv,
                () -> UUID.randomUUID().toString(),
                System::nanoTime,
                Instant::now);
    }

    OptimizationApiController(
            LegacyStorage storage,
            LegacyWorkflowExecutor executions,
            Function<String, String> environment,
            Supplier<String> requestIds,
            LongSupplier defaultSeeds,
            Supplier<Instant> clock) {
        this.storage = storage;
        this.executions = executions;
        this.environment = environment;
        this.requestIds = requestIds;
        this.defaultSeeds = defaultSeeds;
        this.clock = clock;
    }

    void register(HttpServer server) {
        server.createContext("/optimizations", this::handleOptimizations);
    }

    void handleOptimizations(HttpExchange exchange) throws IOException {
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

        String requestId = requestIds.get();
        Map<String, Object> parameters = map(body.get("parameters"));
        parameters.put("parallelRuns", bounded(parameters.get("parallelRuns"), 8, 1, 20));
        parameters.put("iterationsPerRun", bounded(parameters.get("iterationsPerRun"), 5_000, 100, 250_000));
        parameters.put("seed", longNumber(parameters.get("seed"), defaultSeeds.getAsLong()));

        Map<String, Object> workflowInput = new LinkedHashMap<>();
        workflowInput.put("requestId", requestId);
        workflowInput.put("inputUri", inputUri);
        workflowInput.put("parameters", parameters);
        workflowInput.put("submittedAt", clock.get().toString());

        executions.createExecution(requiredEnvironment("WORKFLOW_NAME"), JsonSupport.write(workflowInput));

        HttpJson.respond(exchange, 202, Map.of(
                "requestId", requestId,
                "status", "ACCEPTED",
                "statusPath", "/optimizations/" + requestId));
    }

    private void getResult(HttpExchange exchange, String requestId) throws IOException {
        if (requestId.isBlank() || requestId.contains("/")) {
            throw new IllegalArgumentException("requestId is required");
        }
        byte[] bytes = storage.readResult(requiredEnvironment("RESULTS_BUCKET"), "results/" + requestId + ".json");
        if (bytes == null) {
            HttpJson.respond(exchange, 202, Map.of("requestId", requestId, "status", "RUNNING"));
            return;
        }
        exchange.getResponseHeaders().set("content-type", "application/json; charset=utf-8");
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

    private String requiredEnvironment(String name) {
        String value = environment.apply(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is not configured");
        }
        return value;
    }
}
