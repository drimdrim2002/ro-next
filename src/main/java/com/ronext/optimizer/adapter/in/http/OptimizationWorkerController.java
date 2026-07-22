package com.ronext.optimizer.adapter.in.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
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

final class OptimizationWorkerController {
    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private final AlnsBatchEngine engine = new AlnsBatchEngine();

    void register(HttpServer server) {
        server.createContext("/internal/batches", this::runBatch);
        server.createContext("/internal/finalize", this::finalizeResult);
    }

    private void runBatch(HttpExchange exchange) throws IOException {
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
            storage.create(BlobInfo.newBuilder(requiredEnvironment("RESULTS_BUCKET"),
                            "candidates/" + requestId + "/" + runNumber + ".json")
                    .setContentType("application/json")
                    .build(), JsonSupport.write(candidate).getBytes());
            HttpJson.respond(exchange, 200, Map.of("requestId", requestId, "runNumber", runNumber, "status", "CANDIDATE_STORED"));
        } catch (IllegalArgumentException exception) {
            HttpJson.error(exchange, 400, exception.getMessage());
        } catch (Exception exception) {
            HttpJson.error(exchange, 500, "Unable to run ALNS batch");
        }
    }

    private void finalizeResult(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            HttpJson.error(exchange, 405, "Method not allowed");
            return;
        }
        try {
            Map<String, Object> input = JsonSupport.MAPPER.readValue(exchange.getRequestBody(), Map.class);
            String requestId = requiredString(input, "requestId");
            String bucket = requiredEnvironment("RESULTS_BUCKET");
            List<Map<String, Object>> candidates = new ArrayList<>();
            for (var blob : storage.list(bucket, Storage.BlobListOption.prefix("candidates/" + requestId + "/")).iterateAll()) {
                candidates.add(readCandidate(blob.getBlobId()));
            }
            Map<String, Object> best = candidates.stream()
                    .min(Comparator.comparingDouble(candidate -> ((Number) candidate.get("objective")).doubleValue()))
                    .orElseThrow(() -> new IllegalStateException("No ALNS candidate was generated"));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("requestId", requestId);
            result.put("status", "COMPLETED");
            result.put("inputUri", input.get("inputUri"));
            result.put("bestCandidate", best);
            result.put("completedAt", Instant.now().toString());
            storage.create(BlobInfo.newBuilder(bucket, "results/" + requestId + ".json")
                    .setContentType("application/json")
                    .build(), JsonSupport.write(result).getBytes());
            HttpJson.respond(exchange, 200, result);
        } catch (IllegalArgumentException exception) {
            HttpJson.error(exchange, 400, exception.getMessage());
        } catch (Exception exception) {
            HttpJson.error(exchange, 500, "Unable to finalize optimization");
        }
    }

    private Map<String, Object> readCandidate(BlobId blobId) {
        try {
            return JsonSupport.MAPPER.readValue(storage.readAllBytes(blobId), new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read candidate " + blobId.getName(), exception);
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

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is not configured");
        }
        return value;
    }
}
