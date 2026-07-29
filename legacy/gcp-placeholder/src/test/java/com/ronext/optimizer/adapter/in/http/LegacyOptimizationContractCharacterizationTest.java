package com.ronext.optimizer.adapter.in.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ronext.optimizer.application.AlnsBatchEngine;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LegacyOptimizationContractCharacterizationTest {
    private static final Instant FIXED_TIME = Instant.parse("2026-07-28T00:00:00Z");

    private final FakeStorage storage = new FakeStorage();
    private final FakeWorkflow workflow = new FakeWorkflow();
    private final Map<String, String> environment = Map.of(
            "RESULTS_BUCKET", "result-bucket",
            "WORKFLOW_NAME", "projects/p/locations/l/workflows/w");
    private OptimizationApiController api;
    private OptimizationWorkerController worker;

    @BeforeEach
    void createControllers() {
        api = new OptimizationApiController(
                storage,
                workflow,
                environment::get,
                () -> "fixed-request-id",
                () -> 123_456_789L,
                () -> FIXED_TIME);
        worker = new OptimizationWorkerController(
                storage,
                new AlnsBatchEngine(),
                environment::get,
                () -> FIXED_TIME);
    }

    @Test
    void exposesCurrentPublicAndInternalPathsAndStatusCodes() throws Exception {
        assertEquals(404, request("POST", "/optimizations/unknown", "{}").statusCode());
        assertEquals(400, request("GET", "/optimizations/", null).statusCode());
        assertEquals(405, request("GET", "/internal/batches", null).statusCode());
        assertEquals(405, request("GET", "/internal/finalize", null).statusCode());

        ExchangeResult unsupportedPublicMethod = request("PUT", "/optimizations", "{}");
        assertEquals(404, unsupportedPublicMethod.statusCode());
        assertEquals(Map.of("message", "Not found"), json(unsupportedPublicMethod.body()));
    }

    @Test
    void appliesCurrentParallelRunIterationAndSeedDefaultsAndClamps() throws Exception {
        ExchangeResult defaultResponse = request(
                "POST",
                "/optimizations",
                "{\"inputUri\":\"gs://inputs/case.json\"}");

        assertEquals(202, defaultResponse.statusCode());
        assertEquals(
                Map.of(
                        "requestId", "fixed-request-id",
                        "status", "ACCEPTED",
                        "statusPath", "/optimizations/fixed-request-id"),
                json(defaultResponse.body()));

        Map<String, Object> defaultArgument = json(workflow.arguments.getFirst());
        Map<String, Object> defaultParameters = objectMap(defaultArgument.get("parameters"));
        assertEquals(8, defaultParameters.get("parallelRuns"));
        assertEquals(5_000, defaultParameters.get("iterationsPerRun"));
        assertEquals(123_456_789L, ((Number) defaultParameters.get("seed")).longValue());
        assertEquals("2026-07-28T00:00:00Z", defaultArgument.get("submittedAt"));
        assertEquals("projects/p/locations/l/workflows/w", workflow.parents.getFirst());

        ExchangeResult clampedResponse = request(
                "POST",
                "/optimizations",
                """
                {
                  "inputUri": "gs://inputs/case.json",
                  "parameters": {
                    "parallelRuns": 0,
                    "iterationsPerRun": 999999,
                    "seed": 7
                  }
                }
                """);

        assertEquals(202, clampedResponse.statusCode());
        Map<String, Object> clampedArgument = json(workflow.arguments.get(1));
        Map<String, Object> clampedParameters = objectMap(clampedArgument.get("parameters"));
        assertEquals(1, clampedParameters.get("parallelRuns"));
        assertEquals(250_000, clampedParameters.get("iterationsPerRun"));
        assertEquals(7L, ((Number) clampedParameters.get("seed")).longValue());

        ExchangeResult oppositeClampResponse = request(
                "POST",
                "/optimizations",
                """
                {
                  "inputUri": "gs://inputs/case.json",
                  "parameters": {
                    "parallelRuns": 999,
                    "iterationsPerRun": 0
                  }
                }
                """);

        assertEquals(202, oppositeClampResponse.statusCode());
        Map<String, Object> oppositeClampArgument = json(workflow.arguments.get(2));
        Map<String, Object> oppositeClampParameters =
                objectMap(oppositeClampArgument.get("parameters"));
        assertEquals(20, oppositeClampParameters.get("parallelRuns"));
        assertEquals(100, oppositeClampParameters.get("iterationsPerRun"));
    }

    @Test
    void returnsCurrentValidationErrorsAndRedactedWorkflowFailure() throws Exception {
        ExchangeResult missing = request("POST", "/optimizations", "{}");
        assertEquals(400, missing.statusCode());
        assertEquals(Map.of("message", "inputUri is required"), json(missing.body()));

        ExchangeResult wrongScheme = request(
                "POST", "/optimizations", "{\"inputUri\":\"https://example.test/input.json\"}");
        assertEquals(400, wrongScheme.statusCode());
        assertEquals(Map.of("message", "inputUri must be a gs:// URI"), json(wrongScheme.body()));

        workflow.failure = new IllegalStateException("credential-secret-marker");
        ExchangeResult failed = request(
                "POST", "/optimizations", "{\"inputUri\":\"gs://inputs/case.json\"}");
        assertEquals(500, failed.statusCode());
        assertEquals(
                Map.of("message", "Unable to process optimization request"),
                json(failed.body()));
        assertFalse(failed.body().contains("credential-secret-marker"));
    }

    @Test
    void returnsRunningWhenResultObjectIsMissingAndRawResultWhenPresent() throws Exception {
        ExchangeResult running = request("GET", "/optimizations/request-9", null);
        assertEquals(202, running.statusCode());
        assertEquals(
                Map.of("requestId", "request-9", "status", "RUNNING"),
                json(running.body()));
        assertEquals(List.of("results/request-9.json"), storage.readResultKeys);

        storage.putObject(
                "results/request-9.json",
                "{\"requestId\":\"request-9\",\"status\":\"COMPLETED\",\"raw\":true}"
                        .getBytes(StandardCharsets.UTF_8));
        ExchangeResult completed = request("GET", "/optimizations/request-9", null);
        assertEquals(200, completed.statusCode());
        assertEquals(true, json(completed.body()).get("raw"));
    }

    @Test
    void storesCandidateAtCurrentObjectKey() throws Exception {
        ExchangeResult response = request(
                "POST",
                "/internal/batches",
                """
                {
                  "requestId": "request-7",
                  "inputUri": "gs://inputs/case.json",
                  "runNumber": 4,
                  "seed": 46,
                  "iterations": 5000
                }
                """);

        assertEquals(200, response.statusCode());
        assertTrue(storage.containsObject("candidates/request-7/4.json"));
        assertEquals(
                Map.of("requestId", "request-7", "runNumber", 4, "status", "CANDIDATE_STORED"),
                json(response.body()));
        Map<String, Object> stored = json(new String(
                storage.latestContent("candidates/request-7/4.json"), StandardCharsets.UTF_8));
        assertEquals("CANDIDATE", stored.get("status"));
        assertEquals(46L, ((Number) stored.get("seed")).longValue());
    }

    @Test
    void finalizesFromVisiblePrefixAndRawMinimumObjectiveAtCurrentResultKey() throws Exception {
        storage.putObject(
                "candidates/request-7/0.json",
                "{\"objective\":55.5,\"runNumber\":0}".getBytes(StandardCharsets.UTF_8));
        storage.putObject(
                "candidates/request-7/1.json",
                "{\"objective\":12.25,\"runNumber\":1}".getBytes(StandardCharsets.UTF_8));
        storage.putObject(
                "candidates/another-request/0.json",
                "{\"objective\":1.0,\"runNumber\":0}".getBytes(StandardCharsets.UTF_8));

        ExchangeResult response = request(
                "POST",
                "/internal/finalize",
                "{\"requestId\":\"request-7\",\"inputUri\":\"gs://inputs/case.json\"}");

        assertEquals(200, response.statusCode());
        assertEquals(List.of("candidates/request-7/"), storage.listPrefixes);
        Map<String, Object> result = json(response.body());
        assertEquals("COMPLETED", result.get("status"));
        assertEquals(1, objectMap(result.get("bestCandidate")).get("runNumber"));
        assertEquals("2026-07-28T00:00:00Z", result.get("completedAt"));
        assertTrue(storage.containsObject("results/request-7.json"));
    }

    @Test
    void readsTheListedGenerationWhenCandidateIsOverwrittenAfterListing() throws Exception {
        storage.putObject(
                "candidates/request-7/0.json",
                "{\"objective\":12.25,\"runNumber\":0,\"generation\":\"listed\"}"
                        .getBytes(StandardCharsets.UTF_8));
        storage.overwriteAfterListing = () -> storage.putObject(
                "candidates/request-7/0.json",
                "{\"objective\":1.0,\"runNumber\":99,\"generation\":\"newer\"}"
                        .getBytes(StandardCharsets.UTF_8));

        ExchangeResult response = request(
                "POST",
                "/internal/finalize",
                "{\"requestId\":\"request-7\",\"inputUri\":\"gs://inputs/case.json\"}");

        assertEquals(200, response.statusCode());
        Map<String, Object> bestCandidate = objectMap(json(response.body()).get("bestCandidate"));
        assertEquals("listed", bestCandidate.get("generation"));
        assertEquals(0, bestCandidate.get("runNumber"));
        assertEquals("newer", objectMap(JsonSupport.MAPPER.readValue(
                        storage.latestContent("candidates/request-7/0.json"), Map.class))
                .get("generation"));
    }

    @Test
    void returnsCurrentValidationAndRedactedStorageOrEmptyCandidateFailures() throws Exception {
        ExchangeResult invalidBatch = request(
                "POST",
                "/internal/batches",
                "{\"requestId\":\"request-7\",\"inputUri\":\"gs://inputs/case.json\"}");
        assertEquals(400, invalidBatch.statusCode());
        assertEquals(Map.of("message", "runNumber must be a number"), json(invalidBatch.body()));

        ExchangeResult empty = request(
                "POST", "/internal/finalize", "{\"requestId\":\"request-7\"}");
        assertEquals(500, empty.statusCode());
        assertEquals(Map.of("message", "Unable to finalize optimization"), json(empty.body()));

        storage.failure = new IllegalStateException("storage-secret-marker");
        ExchangeResult storageFailure = request(
                "POST",
                "/internal/batches",
                """
                {
                  "requestId": "request-7",
                  "inputUri": "gs://inputs/case.json",
                  "runNumber": 0,
                  "seed": 42,
                  "iterations": 100
                }
                """);
        assertEquals(500, storageFailure.statusCode());
        assertEquals(Map.of("message", "Unable to run ALNS batch"), json(storageFailure.body()));
        assertFalse(storageFailure.body().contains("storage-secret-marker"));
    }

    private ExchangeResult request(String method, String path, String body) throws Exception {
        FakeExchange exchange = new FakeExchange(method, path, body);
        if (path.startsWith("/optimizations")) {
            api.handleOptimizations(exchange);
        } else if ("/internal/batches".equals(path)) {
            worker.runBatch(exchange);
        } else if ("/internal/finalize".equals(path)) {
            worker.finalizeResult(exchange);
        } else {
            throw new IllegalArgumentException("Test did not route path " + path);
        }
        return new ExchangeResult(
                exchange.statusCode,
                exchange.responseBody.toString(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> json(String value) throws Exception {
        return JsonSupport.MAPPER.readValue(value, Map.class);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objectMap(Object value) {
        return (Map<String, Object>) value;
    }

    private record ExchangeResult(int statusCode, String body) {
    }

    private static final class FakeExchange extends HttpExchange {
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        private final URI requestUri;
        private final String requestMethod;
        private final InputStream requestBody;
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        private int statusCode = -1;

        private FakeExchange(String requestMethod, String path, String requestBody) {
            this.requestMethod = requestMethod;
            this.requestUri = URI.create(path);
            this.requestBody = new ByteArrayInputStream(
                    requestBody == null ? new byte[0] : requestBody.getBytes(StandardCharsets.UTF_8));
            requestHeaders.set("content-type", "application/json");
        }

        @Override
        public Headers getRequestHeaders() {
            return requestHeaders;
        }

        @Override
        public Headers getResponseHeaders() {
            return responseHeaders;
        }

        @Override
        public URI getRequestURI() {
            return requestUri;
        }

        @Override
        public String getRequestMethod() {
            return requestMethod;
        }

        @Override
        public HttpContext getHttpContext() {
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public InputStream getRequestBody() {
            return requestBody;
        }

        @Override
        public OutputStream getResponseBody() {
            return responseBody;
        }

        @Override
        public void sendResponseHeaders(int responseCode, long responseLength) {
            statusCode = responseCode;
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return new InetSocketAddress("127.0.0.1", 1);
        }

        @Override
        public int getResponseCode() {
            return statusCode;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return new InetSocketAddress("127.0.0.1", 2);
        }

        @Override
        public String getProtocol() {
            return "HTTP/1.1";
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public void setAttribute(String name, Object value) {
        }

        @Override
        public void setStreams(InputStream input, OutputStream output) {
        }

        @Override
        public HttpPrincipal getPrincipal() {
            return null;
        }
    }

    private static final class FakeWorkflow implements LegacyWorkflowExecutor {
        private final List<String> parents = new ArrayList<>();
        private final List<String> arguments = new ArrayList<>();
        private RuntimeException failure;

        @Override
        public void createExecution(String workflowName, String argument) {
            if (failure != null) {
                throw failure;
            }
            parents.add(workflowName);
            arguments.add(argument);
        }
    }

    private static final class FakeStorage implements LegacyStorage {
        private final Map<String, List<VersionedObject>> objects = new LinkedHashMap<>();
        private final List<String> readResultKeys = new ArrayList<>();
        private final List<String> listPrefixes = new ArrayList<>();
        private RuntimeException failure;
        private Runnable overwriteAfterListing;
        private long nextGeneration = 1;

        @Override
        public byte[] readResult(String bucket, String objectKey) {
            failIfConfigured();
            assertEquals("result-bucket", bucket);
            readResultKeys.add(objectKey);
            return containsObject(objectKey) ? latestContent(objectKey) : null;
        }

        @Override
        public void writeCandidate(String bucket, String objectKey, byte[] content) {
            failIfConfigured();
            assertEquals("result-bucket", bucket);
            putObject(objectKey, content);
        }

        @Override
        public List<CandidateObjectReference> listCandidateObjects(String bucket, String prefix) {
            failIfConfigured();
            assertEquals("result-bucket", bucket);
            listPrefixes.add(prefix);
            List<CandidateObjectReference> references = objects.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(prefix))
                    .map(entry -> {
                        VersionedObject listed = entry.getValue().getLast();
                        return (CandidateObjectReference)
                                new FakeCandidateObjectReference(entry.getKey(), listed.generation());
                    })
                    .toList();
            if (overwriteAfterListing != null) {
                Runnable overwrite = overwriteAfterListing;
                overwriteAfterListing = null;
                overwrite.run();
            }
            return references;
        }

        @Override
        public byte[] readCandidate(String bucket, CandidateObjectReference reference) {
            failIfConfigured();
            assertEquals("result-bucket", bucket);
            if (!(reference instanceof FakeCandidateObjectReference fakeReference)) {
                throw new IllegalArgumentException("Unexpected fake candidate reference");
            }
            return objects.get(fakeReference.objectKey()).stream()
                    .filter(version -> version.generation() == fakeReference.generation())
                    .findFirst()
                    .orElseThrow()
                    .content();
        }

        @Override
        public void writeResult(String bucket, String objectKey, byte[] content) {
            failIfConfigured();
            assertEquals("result-bucket", bucket);
            putObject(objectKey, content);
        }

        private void putObject(String objectKey, byte[] content) {
            objects.computeIfAbsent(objectKey, ignored -> new ArrayList<>())
                    .add(new VersionedObject(nextGeneration++, content));
        }

        private boolean containsObject(String objectKey) {
            return objects.containsKey(objectKey) && !objects.get(objectKey).isEmpty();
        }

        private byte[] latestContent(String objectKey) {
            return objects.get(objectKey).getLast().content();
        }

        private void failIfConfigured() {
            if (failure != null) {
                throw failure;
            }
        }

        private record FakeCandidateObjectReference(String objectKey, long generation)
                implements CandidateObjectReference {
        }

        private record VersionedObject(long generation, byte[] content) {
        }
    }
}
