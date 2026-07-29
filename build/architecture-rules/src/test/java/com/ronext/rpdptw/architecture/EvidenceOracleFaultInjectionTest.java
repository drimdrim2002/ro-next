package com.ronext.rpdptw.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EvidenceOracleFaultInjectionTest {
    @TempDir
    Path temporary;

    @Test
    void redactionScannerFailsClosedWhenGrepReturnsExitTwo() throws Exception {
        Path report = temporary.resolve("redaction-fault-report.txt");

        CommandResult result = run(
                "./build/verify-evidence-redaction.sh",
                "--self-test",
                report.toString());

        assertEquals(0, result.exitCode(), result.output());
        String evidence = Files.readString(report);
        assertTrue(evidence.contains("GREP_EXIT_2_FAULT_INJECTION=PASS"));
        assertTrue(evidence.contains("GREP_EXIT_2_SCAN_EXIT_CODE=2"));
        assertTrue(evidence.contains("GREP_EXIT_2_PASS_REPORT_PRESENT=NO"));
        assertTrue(evidence.contains("CLEAN_NO_DETECTION_SCAN=PASS"));
        assertFalse(evidence.contains("PHASE00_CANARY_CREDENTIAL_VALUE"));
        assertFalse(evidence.contains("PHASE00_CANARY_PII_VALUE"));
    }

    @Test
    void sourceScannerFailsClosedWhenRgReturnsExitTwo() throws Exception {
        Path report = temporary.resolve("source-scan-fault-report.txt");

        CommandResult result = run(
                "./build/verify-phase-00-source-scans.sh",
                "--self-test",
                report.toString());

        assertEquals(0, result.exitCode(), result.output());
        String evidence = Files.readString(report);
        assertTrue(evidence.contains("FAULT_TEST_NAME=rgExitTwoIsExecutionFailure"));
        assertTrue(evidence.contains("INJECTED_DETECTOR_EXIT_CODE=2"));
        assertTrue(evidence.contains("SCAN_COMMAND_EXIT_CODE=2"));
        assertTrue(evidence.contains("MATCH_RESULT=NOT_EVALUATED"));
        assertTrue(evidence.contains("PASS_REPORT_PRESENT=NO"));
    }

    @Test
    void legacyComparatorPreservesSiblingOrderAndRejectsFiveMutations() throws Exception {
        Path report = temporary.resolve("legacy-comparator-fault-report.txt");

        CommandResult result = run(
                "./build/compare-legacy-baseline.sh",
                "--self-test",
                report.toString());

        assertEquals(0, result.exitCode(), result.output());
        String evidence = Files.readString(report);
        assertTrue(evidence.contains(
                "NEGATIVE_TEST_NAME_1=duplicateDependencyMultiplicityMutationIsRejected"));
        assertTrue(evidence.contains(
                "NEGATIVE_TEST_NAME_2=directChildToNestedChildReparentMutationIsRejected"));
        assertTrue(evidence.contains(
                "NEGATIVE_TEST_NAME_3=conflictAnnotationMutationIsRejected"));
        assertTrue(evidence.contains(
                "NEGATIVE_TEST_NAME_4=repeatedTransitiveMultiplicityMutationIsRejected"));
        assertTrue(evidence.contains(
                "NEGATIVE_TEST_NAME_5=dependencyCoordinateMutationIsRejected"));
        assertTrue(evidence.contains("AUTOMATED_COMPARATOR_NEGATIVE_TEST_COUNT=5"));
        assertTrue(evidence.contains("AUTOMATED_COMPARATOR_TOTAL_CASE_COUNT=6"));
        assertTrue(evidence.contains("FALSE_GREEN_COUNT=0"));
    }

    @Test
    void allGateCriticalDetectorAndTraversalFaultsFailClosed() throws Exception {
        Path report = temporary.resolve("gate-detector-fault-report.txt");

        CommandResult result = run(
                "./build/test-gate-detector-faults.sh",
                report.toString());

        assertEquals(0, result.exitCode(), result.output());
        String evidence = Files.readString(report);
        assertTrue(evidence.contains("PHASE00_GATE_DETECTOR_FAULT_SELF_TEST=PASS"));
        assertTrue(evidence.contains("CASE_COUNT=51"));
        assertTrue(evidence.contains("ACTUAL_INVENTORY_CLASS_COUNT=30"));
        assertTrue(evidence.contains("REVIEW05_NEW_FAULT_CASE_COUNT=28"));
        assertTrue(evidence.contains("STALE_PASS_INVALIDATION_CASE_COUNT=9"));
        assertTrue(evidence.contains("ACTUAL_CALL_PATH_CASE_COUNT=12"));
        assertTrue(evidence.contains(
                "FAKE_SUCCESS_OUTPUT_THEN_NONZERO_CASE_COUNT=28"));
        assertTrue(evidence.contains("GREP_EXIT_2_CASE_COUNT=4"));
        assertTrue(evidence.contains("GREP_EXIT_3_CASE_COUNT=6"));
        assertTrue(evidence.contains("RG_EXIT_2_CASE_COUNT=1"));
        assertTrue(evidence.contains("FIND_EXIT_9_CASE_COUNT=1"));
        assertTrue(evidence.contains("JDEPS_EXIT_9_CASE_COUNT=1"));
        assertTrue(evidence.contains("SHASUM_EXIT_9_CASE_COUNT=1"));
        assertTrue(evidence.contains("SORT_EXIT_9_CASE_COUNT=1"));
        assertTrue(evidence.contains("AWK_EXIT_9_CASE_COUNT=1"));
        assertTrue(evidence.contains("WC_EXIT_9_CASE_COUNT=1"));
        assertTrue(evidence.contains("LS_EXIT_9_CASE_COUNT=1"));
        assertTrue(evidence.contains("CMP_EXIT_9_CASE_COUNT=1"));
        assertTrue(evidence.contains("JAR_EXIT_9_CASE_COUNT=1"));
        assertTrue(evidence.contains("ENV_LAUNCHER_EXIT_125_CASE_COUNT=1"));
        assertTrue(evidence.contains("MKTEMP_EXIT_9_CASE_COUNT=2"));
        assertTrue(evidence.contains("MV_EXIT_9_CASE_COUNT=3"));
        assertTrue(evidence.contains("FIRST_SECOND_SEAL_PUBLICATION_FAULT_CASE_COUNT=2"));
        assertTrue(evidence.contains("UNZIP_EXIT_9_CASE_COUNT=2"));
        assertTrue(evidence.contains("MVNW_EXIT_9_CASE_COUNT=1"));
        assertTrue(evidence.contains("NOT_EVALUATED_CASE_COUNT=49"));
        assertTrue(evidence.contains("FALSE_GREEN_COUNT=0"));
    }

    @Test
    void recursiveMavenWrapperAndLauncherFaultsFailClosed() throws Exception {
        Path report = temporary.resolve("maven-wrapper-fault-report.txt");

        CommandResult result = run(
                "./build/test-maven-wrapper-faults.sh",
                report.toString());

        assertEquals(0, result.exitCode(), result.output());
        String evidence = Files.readString(report);
        assertTrue(evidence.contains("PHASE00_MAVEN_WRAPPER_FAULT_SELF_TEST=PASS"));
        assertTrue(evidence.contains("CASE_COUNT=28"));
        assertTrue(evidence.contains("ACTUAL_PATH_FAULT_CASE_COUNT=14"));
        assertTrue(evidence.contains("ACTUAL_PREREQUISITE_CLASS_COUNT=13"));
        assertTrue(evidence.contains("CONDITIONAL_PATH_FAULT_CASE_COUNT=14"));
        assertTrue(evidence.contains("MAVENRC_SOURCE_ERROR_CASE_COUNT=1"));
        assertTrue(evidence.contains("PRIVATE_TEST_DRIVER_FAULT_CASE_COUNT=20"));
        assertTrue(evidence.contains("PUBLIC_TEST_OVERRIDE_ATTACK_COUNT=14"));
        assertTrue(evidence.contains(
                "PUBLIC_PRODUCTION_TEST_OVERRIDE_REFERENCE_COUNT=0"));
        assertTrue(evidence.contains("PUBLIC_PRIVATE_DRIVER_SELECTION_PATH=NONE"));
        assertTrue(evidence.contains("EXACT_EXIT_41_CASE_COUNT=22"));
        assertTrue(evidence.contains("DOWNSTREAM_MAVEN_EXECUTION_COUNT=0"));
        assertTrue(evidence.contains("FALSE_GREEN_COUNT=0"));
    }

    @Test
    void pinnedMavenNormalLauncherContractMatchesOfficialDistribution() throws Exception {
        Path report = temporary.resolve("maven-launcher-parity-report.txt");

        CommandResult result = run(
                "./build/test-maven-launcher-parity.sh",
                report.toString());

        assertEquals(0, result.exitCode(), result.output());
        String evidence = Files.readString(report);
        assertTrue(evidence.contains("PHASE00_MAVEN_3_9_14_LAUNCHER_PARITY=PASS"));
        assertTrue(evidence.contains("EXACT_AB_PAIR_COUNT=18"));
        assertTrue(evidence.contains("SOURCE_FAILURE_PAIR_COUNT=1"));
        assertTrue(evidence.contains("FAIL_CLOSED_ENRICHMENT_PAIR_COUNT=1"));
        assertTrue(evidence.contains("CONTROLLED_SYSTEM_RC_SEAM_COUNT=2"));
        assertTrue(evidence.contains("FALSE_GREEN_COUNT=0"));
    }

    @Test
    void gitWhitespaceExitBitsRemainSemantic() throws Exception {
        Path report = temporary.resolve("git-whitespace-semantics-report.txt");

        CommandResult result = run(
                "./build/test-git-whitespace-semantics.sh",
                report.toString());

        assertEquals(0, result.exitCode(), result.output());
        String evidence = Files.readString(report);
        assertTrue(evidence.contains("PHASE00_GIT_WHITESPACE_SEMANTICS_SELF_TEST=PASS"));
        assertTrue(evidence.contains("CASE_COUNT=6"));
        assertTrue(evidence.contains("TRACKED_WHITESPACE_RAW_EXIT=2"));
        assertTrue(evidence.contains("NO_INDEX_ORDINARY_DIFFERENCE_RAW_EXIT=1"));
        assertTrue(evidence.contains("NO_INDEX_WHITESPACE_RAW_EXIT=3"));
        assertTrue(evidence.contains("EXECUTION_ERROR_CASE_COUNT=2"));
        assertTrue(evidence.contains("FALSE_GREEN_COUNT=0"));
    }

    @Test
    void recursiveToolInventoryPinsAllShellSourcesAndLauncher() throws Exception {
        Path report = temporary.resolve("recursive-tool-inventory-report.txt");
        Path receipt = temporary.resolve("recursive-tool-inventory-receipt.txt");
        String token = "architecture-junit-" + UUID.randomUUID();

        CommandResult producer = run(
                "./build/run-verify-gate-tool-inventory.sh",
                report.toString(),
                receipt.toString(),
                token);
        assertEquals(0, producer.exitCode(), producer.output());
        String reportDigest = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                        .digest(Files.readAllBytes(report)));
        List<String> receiptFields =
                Files.readAllLines(receipt, StandardCharsets.UTF_8);
        assertEquals(5, receiptFields.size());
        assertEquals("PHASE00_GATE_TOOL_INVENTORY_RECEIPT=PASS",
                receiptFields.get(0));
        assertEquals("INVOCATION_TOKEN=" + token, receiptFields.get(1));
        assertEquals("REPORT_SHA256=" + reportDigest, receiptFields.get(2));
        assertEquals("PRODUCER_RESULT=PASS", receiptFields.get(3));
        assertEquals("PRODUCER_EXIT_CODE=0", receiptFields.get(4));

        CommandResult consumer = run(
                "./build/run-verify-gate-tool-inventory.sh",
                "--consume",
                report.toString(),
                receipt.toString(),
                token,
                Integer.toString(producer.exitCode()));
        assertEquals(0, consumer.exitCode(), consumer.output());
        String evidence = Files.readString(report);
        assertTrue(evidence.contains("PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS"));
        assertTrue(evidence.contains("INVENTORY_ROW_COUNT=54"));
        assertTrue(evidence.contains("WRAPPER_LAUNCHER_ACTUAL_CLASS_COUNT=13"));
        assertTrue(evidence.contains("WRAPPER_LAUNCHER_CONDITIONAL_CLASS_COUNT=14"));
        assertTrue(evidence.contains("PINNED_LAUNCHER_CONTRACT_ROW_COUNT=19"));
        assertTrue(evidence.contains("STATIC_SOURCE_SNAPSHOT=COMPLETE"));
        assertTrue(evidence.contains(
                "PUBLIC_PRODUCTION_TEST_OVERRIDE_REFERENCE_COUNT=0"));
        assertTrue(evidence.contains("PRIVATE_TEST_DRIVER_PUBLIC_SELECTION_PATH=NONE"));
        assertTrue(evidence.contains("UNCLASSIFIED_COMMAND_MUTATION_CASE_COUNT=1"));
        assertTrue(evidence.contains("UNCLASSIFIED_EXTERNAL_COMMAND_COUNT=0"));
        assertTrue(evidence.contains("UNTESTED_WRAPPER_LAUNCHER_CLASS_COUNT=0"));
    }

    private static CommandResult run(String... command) throws Exception {
        Process process = new ProcessBuilder(command)
                .directory(ArchitecturePolicy.repository().toFile())
                .redirectErrorStream(true)
                .start();
        String output =
                new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        return new CommandResult(exitCode, output);
    }

    private record CommandResult(int exitCode, String output) {}
}
