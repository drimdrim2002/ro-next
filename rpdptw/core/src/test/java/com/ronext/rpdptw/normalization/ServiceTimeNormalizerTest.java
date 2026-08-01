package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.CanonicalItemInput;
import com.ronext.rpdptw.input.CanonicalServiceInput;
import com.ronext.rpdptw.input.ExternalLocationId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceTimeNormalizerTest {

    private final ServiceTimeNormalizer normalizer = new DefaultServiceTimeNormalizer();

    @Test
    void checksServiceDurationItemTimeQuantitySum() {
        CanonicalServiceInput service = new CanonicalServiceInput(
                new ExternalLocationId("LOC1"),
                "2026-08-01 08:00:00",
                "2026-08-01 17:00:00",
                "300",
                Optional.empty(),
                Optional.empty()
        );
        List<CanonicalItemInput> items = List.of(
                new CanonicalItemInput("1.0", "0.1", 2, "30"),
                new CanonicalItemInput("2.0", "0.2", 3, "10")
        );
        // duration (300) + 2*30 (60) + 3*10 (30) = 390
        long serviceSeconds = normalizer.calculateServiceSeconds(service, Optional.empty(), items);
        assertEquals(390L, serviceSeconds);
    }

    @Test
    void detectsServiceTimeOverflow() {
        CanonicalServiceInput service = new CanonicalServiceInput(
                new ExternalLocationId("LOC1"),
                "2026-08-01 08:00:00",
                "2026-08-01 17:00:00",
                "100",
                Optional.empty(),
                Optional.empty()
        );
        List<CanonicalItemInput> items = List.of(
                new CanonicalItemInput("1.0", "0.1", 2, String.valueOf(Long.MAX_VALUE))
        );
        TemporalReject reject = assertThrows(TemporalReject.class, () ->
                normalizer.calculateServiceSeconds(service, Optional.empty(), items)
        );
        assertEquals(InputProblemCode.ARITHMETIC_OVERFLOW, reject.code());
    }

    @Test
    void rejectsOrderLevelTaskTime() {
        CanonicalServiceInput service = new CanonicalServiceInput(
                new ExternalLocationId("LOC1"),
                "2026-08-01 08:00:00",
                "2026-08-01 17:00:00",
                "300",
                Optional.empty(),
                Optional.empty()
        );
        TemporalReject reject = assertThrows(TemporalReject.class, () ->
                normalizer.calculateServiceSeconds(service, Optional.of("60"), List.of())
        );
        assertEquals(InputProblemCode.ORDER_LEVEL_TASK_TIME_NOT_ALLOWED, reject.code());
    }
}
