package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.CanonicalPlanEnvelope;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface TimeNormalizer {
    LocalDateTime parseLocalDateTime(String text);

    long toPlanOriginSeconds(LocalDateTime target, LocalDateTime planStart);

    long toPlanOriginSeconds(String dateTimeStr, String planStartStr);

    NormalizedPlanEnvelope normalizePlanEnvelope(CanonicalPlanEnvelope envelope);

    NormalizedWindow normalizeWindow(String windowOpenStr, String windowCloseInclusiveStr, String planStartStr);

    List<NormalizedWindow> expandOvernightWindow(LocalTime openTime, LocalTime closeTime, NormalizedPlanEnvelope planEnvelope, LocalDateTime planStart);

    WorkArcPolicy workArcPolicy();
}
