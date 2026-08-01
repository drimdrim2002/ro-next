package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.CanonicalPlanEnvelope;
import com.ronext.rpdptw.input.ExternalPlanId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeNormalizerTest {

    private final TimeNormalizer normalizer = new DefaultTimeNormalizer();

    @Test
    void convertsExactLocalDateTimeToPlanOriginSeconds() {
        LocalDateTime planStart = LocalDateTime.of(2026, 8, 1, 0, 0, 0);
        LocalDateTime target = LocalDateTime.of(2026, 8, 1, 10, 0, 0);
        long relativeSeconds = normalizer.toPlanOriginSeconds(target, planStart);
        assertEquals(36000L, relativeSeconds);

        long parsedSeconds = normalizer.toPlanOriginSeconds("2026-08-01 10:00:00", "2026-08-01 00:00:00");
        assertEquals(36000L, parsedSeconds);
    }

    @Test
    void rejectsOffsetOrZone() {
        TemporalReject rejectZ = assertThrows(TemporalReject.class, () -> normalizer.parseLocalDateTime("2026-08-01T00:00:00Z"));
        assertEquals(InputProblemCode.TIMEZONE_OR_OFFSET_NOT_ALLOWED, rejectZ.code());

        TemporalReject rejectOffset = assertThrows(TemporalReject.class, () -> normalizer.parseLocalDateTime("2026-08-01 00:00:00+09:00"));
        assertEquals(InputProblemCode.TIMEZONE_OR_OFFSET_NOT_ALLOWED, rejectOffset.code());
    }

    @Test
    void keepsPlanEndExclusiveAndCloseInclusive() {
        CanonicalPlanEnvelope envelope = new CanonicalPlanEnvelope(
                new ExternalPlanId("PLAN1"),
                "CUST1",
                "PROF1",
                "1.0",
                Optional.empty(),
                "2026-08-01 00:00:00",
                "2026-08-04 00:00:00"
        );
        NormalizedPlanEnvelope normalizedEnvelope = normalizer.normalizePlanEnvelope(envelope);
        assertEquals(259200L, normalizedEnvelope.planDurationSeconds());
        assertEquals(259200L, normalizedEnvelope.planEndExclusiveSeconds());

        NormalizedWindow window = normalizer.normalizeWindow("2026-08-01 08:00:00", "2026-08-01 17:00:00", "2026-08-01 00:00:00");
        assertEquals(28800L, window.startSecond());
        assertEquals(61201L, window.endSecondExclusive()); // 17:00:00 is 61200, closeInclusive + 1 = 61201
    }

    @Test
    void expandsOvernightWindowOncePerPlanDate() {
        CanonicalPlanEnvelope envelope = new CanonicalPlanEnvelope(
                new ExternalPlanId("PLAN1"),
                "CUST1",
                "PROF1",
                "1.0",
                Optional.empty(),
                "2026-08-01 00:00:00",
                "2026-08-04 00:00:00" // 3 days: Aug 1, Aug 2, Aug 3
        );
        NormalizedPlanEnvelope normalizedEnvelope = normalizer.normalizePlanEnvelope(envelope);

        List<NormalizedWindow> windows = normalizer.expandOvernightWindow(
                LocalTime.of(22, 0, 0),
                LocalTime.of(2, 0, 0),
                normalizedEnvelope,
                LocalDateTime.of(2026, 8, 1, 0, 0, 0)
        );

        assertEquals(3, windows.size());
        // Date 1 (Aug 1): 22:00 (79200) to Aug 2 02:00 inclusive (93600), endExclusive = 93601
        assertEquals(new NormalizedWindow(79200L, 93601L), windows.get(0));
        // Date 2 (Aug 2): 165600 to 180001
        assertEquals(new NormalizedWindow(165600L, 180001L), windows.get(1));
        // Date 3 (Aug 3): 252000 to Aug 4 02:00 (266401 clipped to planEndExclusive 259200)
        assertEquals(new NormalizedWindow(252000L, 259200L), windows.get(2));
    }

    @Test
    void rejectsEqualOpenCloseWithoutSchemaMeaning() {
        TemporalReject reject = assertThrows(TemporalReject.class, () ->
                normalizer.normalizeWindow("2026-08-01 08:00:00", "2026-08-01 08:00:00", "2026-08-01 00:00:00")
        );
        assertEquals(InputProblemCode.AMBIGUOUS_WINDOW, reject.code());

        TemporalReject rejectOvernight = assertThrows(TemporalReject.class, () ->
                normalizer.expandOvernightWindow(LocalTime.of(8, 0), LocalTime.of(8, 0),
                        new NormalizedPlanEnvelope(new ExternalPlanId("P"), "C", "P", "1", Optional.empty(), 0, 86400, 86400),
                        LocalDateTime.of(2026, 8, 1, 0, 0))
        );
        assertEquals(InputProblemCode.AMBIGUOUS_WINDOW, rejectOvernight.code());
    }

    @Test
    void preservesFullArcRestartPolicyAndExactHandoffOracle() {
        assertEquals(WorkArcPolicy.FULL_ARC_WITHIN_ONE_WORK_WINDOW, normalizer.workArcPolicy());
    }
}
