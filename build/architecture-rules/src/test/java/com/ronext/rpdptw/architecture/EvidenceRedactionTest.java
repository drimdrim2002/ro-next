package com.ronext.rpdptw.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EvidenceRedactionTest {
    private static final String CREDENTIAL_CANARY = "PHASE00_CANARY_CREDENTIAL_VALUE";
    private static final String PII_CANARY = "PHASE00_CANARY_PII_VALUE";

    @TempDir
    Path temporary;

    @Test
    void bundleContainsNoCredentialSecretOrRawPii() throws Exception {
        Files.writeString(
                temporary.resolve("safe-report.txt"),
                "credential-control=redacted\npii-control=redacted\n");

        assertEquals(Map.of(), knownMarkerCounts(temporary));
    }

    @Test
    void knownMarkerCanaryScannerReportsOnlyTokenIdsAndCounts() throws Exception {
        Files.writeString(temporary.resolve("credential-canary.txt"), CREDENTIAL_CANARY);
        Files.writeString(temporary.resolve("pii-canary.txt"), PII_CANARY);

        assertEquals(
                Map.of(
                        "CREDENTIAL_CANARY_TOKEN_ID", 1,
                        "PII_CANARY_TOKEN_ID", 1),
                knownMarkerCounts(temporary));
    }

    private static Map<String, Integer> knownMarkerCounts(Path root) throws IOException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Path file : ArchitecturePolicy.findFiles(root, Files::isRegularFile)) {
            String content = Files.readString(file);
            count(content, CREDENTIAL_CANARY, "CREDENTIAL_CANARY_TOKEN_ID", counts);
            count(content, PII_CANARY, "PII_CANARY_TOKEN_ID", counts);
        }
        return Map.copyOf(counts);
    }

    private static void count(
            String content,
            String marker,
            String tokenId,
            Map<String, Integer> counts) {
        int index = 0;
        while ((index = content.indexOf(marker, index)) >= 0) {
            counts.merge(tokenId, 1, Integer::sum);
            index += marker.length();
        }
    }
}
