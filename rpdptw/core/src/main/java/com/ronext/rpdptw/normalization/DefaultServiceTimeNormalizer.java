package com.ronext.rpdptw.normalization;

import com.ronext.rpdptw.input.CanonicalItemInput;
import com.ronext.rpdptw.input.CanonicalServiceInput;

import java.util.List;
import java.util.Optional;

public class DefaultServiceTimeNormalizer implements ServiceTimeNormalizer {

    @Override
    public long calculateServiceSeconds(CanonicalServiceInput service, Optional<String> orderTaskTime, List<CanonicalItemInput> items) {
        if (service == null) {
            throw new TemporalReject(InputProblemCode.INVALID_SERVICE_PATTERN, "Service input must not be null");
        }
        return calculateServiceSeconds(service.durationSeconds(), orderTaskTime, items);
    }

    @Override
    public long calculateServiceSeconds(String durationSecondsStr, Optional<String> orderTaskTime, List<CanonicalItemInput> items) {
        if (orderTaskTime != null && orderTaskTime.isPresent()) {
            throw new TemporalReject(InputProblemCode.ORDER_LEVEL_TASK_TIME_NOT_ALLOWED, "Order-level task time is not allowed");
        }

        if (durationSecondsStr == null) {
            throw new TemporalReject(InputProblemCode.INVALID_DATETIME, "durationSeconds must not be null");
        }

        long serviceSeconds;
        try {
            serviceSeconds = Long.parseLong(durationSecondsStr.trim());
        } catch (NumberFormatException e) {
            throw new TemporalReject(InputProblemCode.INVALID_DATETIME, "Invalid durationSeconds lexeme: " + durationSecondsStr);
        }

        if (items != null) {
            for (CanonicalItemInput item : items) {
                long itemTaskTime;
                try {
                    itemTaskTime = Long.parseLong(item.itemTaskTimeSeconds().trim());
                } catch (NumberFormatException e) {
                    throw new TemporalReject(InputProblemCode.INVALID_DATETIME, "Invalid itemTaskTimeSeconds: " + item.itemTaskTimeSeconds());
                }

                try {
                    long itemTotal = Math.multiplyExact(itemTaskTime, (long) item.quantity());
                    serviceSeconds = Math.addExact(serviceSeconds, itemTotal);
                } catch (ArithmeticException e) {
                    throw new TemporalReject(InputProblemCode.ARITHMETIC_OVERFLOW, "Arithmetic overflow during service time calculation");
                }
            }
        }

        return serviceSeconds;
    }
}
