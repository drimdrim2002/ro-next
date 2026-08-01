package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.CanonicalPlanEnvelope;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DefaultTimeNormalizer implements TimeNormalizer {

    private static final DateTimeFormatter STRICT_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final Pattern OFFSET_OR_ZONE_PATTERN = Pattern.compile(".*[Zz]|.*[+-]\\d{2}:?\\d{2}|.*\\[.*\\]");

    @Override
    public LocalDateTime parseLocalDateTime(String text) {
        if (text == null) {
            throw new TemporalReject(InputProblemCode.INVALID_DATETIME, "Date-time string must not be null");
        }
        if (OFFSET_OR_ZONE_PATTERN.matcher(text).matches() || text.contains("Z") || text.contains("z") || text.contains("+")) {
            throw new TemporalReject(InputProblemCode.TIMEZONE_OR_OFFSET_NOT_ALLOWED, "Timezone or offset is not allowed in input: " + text);
        }
        try {
            return LocalDateTime.parse(text, STRICT_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new TemporalReject(InputProblemCode.INVALID_DATETIME, "Invalid date-time format (expected yyyy-MM-dd HH:mm:ss): " + text);
        }
    }

    @Override
    public long toPlanOriginSeconds(LocalDateTime target, LocalDateTime planStart) {
        if (target == null || planStart == null) {
            throw new TemporalReject(InputProblemCode.INVALID_DATETIME, "Target and planStart must not be null");
        }
        return Duration.between(planStart, target).getSeconds();
    }

    @Override
    public long toPlanOriginSeconds(String dateTimeStr, String planStartStr) {
        LocalDateTime target = parseLocalDateTime(dateTimeStr);
        LocalDateTime planStart = parseLocalDateTime(planStartStr);
        return toPlanOriginSeconds(target, planStart);
    }

    @Override
    public NormalizedPlanEnvelope normalizePlanEnvelope(CanonicalPlanEnvelope envelope) {
        LocalDateTime startLdt = parseLocalDateTime(envelope.planStart());
        LocalDateTime endLdt = parseLocalDateTime(envelope.planEndExclusive());

        if (!endLdt.isAfter(startLdt)) {
            throw new TemporalReject(InputProblemCode.INVALID_PLAN_RANGE, "planEndExclusive must be strictly after planStart");
        }

        long planStartEpochSecond = startLdt.toEpochSecond(ZoneOffset.UTC);
        long durationSeconds = Duration.between(startLdt, endLdt).getSeconds();

        return new NormalizedPlanEnvelope(
                envelope.planId(),
                envelope.customer(),
                envelope.profile(),
                envelope.profileVersion(),
                envelope.preset(),
                planStartEpochSecond,
                durationSeconds,
                durationSeconds
        );
    }

    @Override
    public NormalizedWindow normalizeWindow(String windowOpenStr, String windowCloseInclusiveStr, String planStartStr) {
        LocalDateTime planStartLdt = parseLocalDateTime(planStartStr);
        LocalDateTime openLdt = parseLocalDateTime(windowOpenStr);
        LocalDateTime closeInclusiveLdt = parseLocalDateTime(windowCloseInclusiveStr);

        long startSec = Duration.between(planStartLdt, openLdt).getSeconds();
        long closeInclusiveSec = Duration.between(planStartLdt, closeInclusiveLdt).getSeconds();

        if (startSec == closeInclusiveSec) {
            throw new TemporalReject(InputProblemCode.AMBIGUOUS_WINDOW, "open and closeInclusive cannot be equal");
        }

        long endExclusiveSec = closeInclusiveSec + 1;
        if (startSec >= endExclusiveSec) {
            throw new TemporalReject(InputProblemCode.AMBIGUOUS_WINDOW, "startSecond must be strictly before endSecondExclusive");
        }

        return new NormalizedWindow(startSec, endExclusiveSec);
    }

    @Override
    public List<NormalizedWindow> expandOvernightWindow(LocalTime openTime, LocalTime closeTime, NormalizedPlanEnvelope planEnvelope, LocalDateTime planStart) {
        if (openTime == null || closeTime == null || planEnvelope == null || planStart == null) {
            throw new TemporalReject(InputProblemCode.INVALID_DATETIME, "Arguments must not be null");
        }
        if (openTime.equals(closeTime)) {
            throw new TemporalReject(InputProblemCode.AMBIGUOUS_WINDOW, "openTime and closeTime cannot be equal");
        }

        long planEndSec = planEnvelope.planEndExclusiveSeconds();
        LocalDateTime planEndLdt = planStart.plusSeconds(planEnvelope.planDurationSeconds());

        List<NormalizedWindow> result = new ArrayList<>();
        LocalDate currentDate = planStart.toLocalDate();
        LocalDate endDate = planEndLdt.toLocalDate();

        while (!currentDate.isAfter(endDate) && Duration.between(planStart, currentDate.atStartOfDay()).getSeconds() < planEndSec) {
            LocalDateTime openLdt = currentDate.atTime(openTime);
            LocalDateTime closeInclusiveLdt;

            if (openTime.isAfter(closeTime)) {
                closeInclusiveLdt = currentDate.plusDays(1).atTime(closeTime);
            } else {
                closeInclusiveLdt = currentDate.atTime(closeTime);
            }

            long openSec = Duration.between(planStart, openLdt).getSeconds();
            long closeInclusiveSec = Duration.between(planStart, closeInclusiveLdt).getSeconds();
            long endExclusiveSec = closeInclusiveSec + 1;

            long clippedStart = Math.max(0L, Math.min(planEndSec, openSec));
            long clippedEnd = Math.max(0L, Math.min(planEndSec, endExclusiveSec));

            if (clippedStart < clippedEnd) {
                result.add(new NormalizedWindow(clippedStart, clippedEnd));
            }

            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    @Override
    public WorkArcPolicy workArcPolicy() {
        return WorkArcPolicy.FULL_ARC_WITHIN_ONE_WORK_WINDOW;
    }
}
