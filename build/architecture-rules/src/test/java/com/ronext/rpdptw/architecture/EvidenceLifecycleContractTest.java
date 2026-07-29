package com.ronext.rpdptw.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EvidenceLifecycleContractTest {
    private static final Set<String> ALLOWLIST = Set.of(
            "phase",
            "canonicalPhasePlanDigest",
            "reviewCriteriaDigest",
            "sourceCommitDigest",
            "inputArtifactDigests",
            "configProfileBuildRuntimeDigests",
            "commandEnvironmentToolchainExitCodeRecordDigest",
            "testResultAndFixtureDigests",
            "requiredEvidenceKeyArtifactDigests",
            "architectureDependencySecurityReportDigests",
            "openGatedDeferredSnapshotDigest",
            "handoffCandidateArtifactDigest",
            "rollbackPointDigest");

    @Test
    void canonicalPreReviewManifestAllowlistExcludesReviewAndAcceptanceFields() throws Exception {
        Path plan = ArchitecturePolicy.repository()
                .resolve("docs/implementation/master-realization-plan.md");
        String source = Files.readString(plan);
        String block = between(
                source,
                "<!-- PRE_REVIEW_MANIFEST_ALLOWLIST_BEGIN -->",
                "<!-- PRE_REVIEW_MANIFEST_ALLOWLIST_END -->");

        Set<String> actual = new LinkedHashSet<>();
        for (String line : block.lines().toList()) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()
                    && !trimmed.startsWith("```")
                    && !trimmed.equals("preReviewEvidenceManifest:")) {
                actual.add(trimmed);
            }
        }

        assertEquals(ALLOWLIST, actual);
        assertFalse(block.contains("reviewerIdentity"));
        assertFalse(block.contains("reviewVerdict"));
        assertFalse(block.contains("independentReviewRef"));
        assertFalse(block.contains("acceptanceReceipt"));
        assertFalse(block.contains("preReviewEvidenceManifestDigest"));
    }

    @Test
    void generatorUsesSeparateCanonicalManifestWithoutReviewOrAcceptanceArtifact() throws Exception {
        String generator = Files.readString(
                ArchitecturePolicy.repository().resolve("build/generate-phase-00-evidence.sh"));

        assertTrue(generator.contains("pre-review-evidence-manifest.yaml"));
        assertTrue(generator.contains("verify-pre-review-evidence-manifest.sh"));
        assertFalse(generator.contains("pre-review-status.txt"));
        assertFalse(generator.contains("independent-review-report."));
        assertFalse(generator.contains("acceptance-receipt."));
    }

    private static String between(String value, String start, String end) {
        int startIndex = value.indexOf(start);
        int endIndex = value.indexOf(end);
        assertTrue(startIndex >= 0);
        assertTrue(endIndex > startIndex);
        return value.substring(startIndex + start.length(), endIndex);
    }
}
