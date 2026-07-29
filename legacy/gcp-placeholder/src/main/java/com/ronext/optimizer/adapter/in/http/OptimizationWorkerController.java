package com.ronext.optimizer.adapter.in.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.cloud.storage.StorageOptions;
import com.ronext.optimizer.application.AlnsBatchEngine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

final class OptimizationWorkerController {
    private final LegacyStorage storage;
    private final AlnsBatchEngine engine;
    private final Function<String, String> environment;
    private final Supplier<Instant> clock;

    OptimizationWorkerController() {
        this(
                new GcpLegacyStorage(StorageOptions.getDefaultInstance().getService()),
                new AlnsBatchEngine(),
                System::getenv,
                Instant::now);
    }

    OptimizationWorkerController(
            LegacyStorage storage,
            AlnsBatchEngine engine,
            Function<String, String> environment,
            Supplier<Instant> clock) {
        this.storage = storage;
        this.engine = engine;
        this.environment = environment;
        this.clock = clock;
    }

    void register(HttpServer server) {
        server.createContext("/internal/batches", this::runBatch);
        server.createContext("/internal/finalize", this::finalizeResult);
    }

    void runBatch(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            HttpJson.error(exchange, 405, "Method not allowed");
            return;
        }
        try {
            Map<String, Object> input = JsonSupport.MAPPER.readValue(exchange.getRequestBody(), Map.class);
            Map<String, Object> candidate = engine.run(
                    requiredString(input, "requestId"),
                    requiredString(input, "inputUri"),
                    number(input, "runNumber"),
                    longNumber(input, "seed"),
                    number(input, "iterations"));
            String requestId = requiredString(input, "requestId");
            int runNumber = number(input, "runNumber");
            storage.writeCandidate(
                    requiredEnvironment("RESULTS_BUCKET"),
                    "candidates/" + requestId + "/" + runNumber + ".json",
                    JsonSupport.write(candidate).getBytes());
            HttpJson.respond(exchange, 200, Map.of("requestId", requestId, "runNumber", runNumber, "status", "CANDIDATE_STORED"));
        } catch (IllegalArgumentException exception) {
            HttpJson.error(exchange, 400, exception.getMessage());
        } catch (Exception exception) {
            HttpJson.error(exchange, 500, "Unable to run ALNS batch");
        }
    }

    void finalizeResult(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            HttpJson.error(exchange, 405, "Method not allowed");
            return;
        }
        try {
            Map<String, Object> input = JsonSupport.MAPPER.readValue(exchange.getRequestBody(), Map.class);
            String requestId = requiredString(input, "requestId");
            String bucket = requiredEnvironment("RESULTS_BUCKET");
            List<Map<String, Object>> candidates = new ArrayList<>();
            for (LegacyStorage.CandidateObjectReference reference
                    : storage.listCandidateObjects(bucket, "candidates/" + requestId + "/")) {
                candidates.add(readCandidate(bucket, reference));
            }
            Map<String, Object> best = candidates.stream()
                    .min(Comparator.comparingDouble(candidate -> ((Number) candidate.get("objective")).doubleValue()))
                    .orElseThrow(() -> new IllegalStateException("No ALNS candidate was generated"));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("requestId", requestId);
            result.put("status", "COMPLETED");
            result.put("inputUri", input.get("inputUri"));
            result.put("bestCandidate", best);
            result.put("completedAt", clock.get().toString());
            storage.writeResult(bucket, "results/" + requestId + ".json", JsonSupport.write(result).getBytes());
            HttpJson.respond(exchange, 200, result);
        } catch (IllegalArgumentException exception) {
            HttpJson.error(exchange, 400, exception.getMessage());
        } catch (Exception exception) {
            HttpJson.error(exchange, 500, "Unable to finalize optimization");
        }
    }

    private Map<String, Object> readCandidate(
            String bucket, LegacyStorage.CandidateObjectReference reference) {
        try {
            return JsonSupport.MAPPER.readValue(
                    storage.readCandidate(bucket, reference),
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to read candidate " + reference.objectKey(), exception);
        }
    }

    private static String requiredString(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return text;
    }

    private static int number(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException(key + " must be a number");
        }
        return number.intValue();
    }

    private static long longNumber(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException(key + " must be a number");
        }
        return number.longValue();
    }

    private String requiredEnvironment(String name) {
        String value = environment.apply(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is not configured");
        }
        return value;
    }
}
