package com.ronext.rpdptw.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Supplier;

public final class Units {

    private Units() {}

    /** 무게·부피 전용. 소수를 받아서 ×1000 후 3자리 FLOOR. 음수 거부. */
    public static long toMilli(BigDecimal value, Supplier<String> field) {
        if (value.signum() < 0) {
            throw new InputException(InputException.Kind.INVALID_INPUT, field.get(), "negative");
        }
        try {
            return value.movePointRight(3).setScale(0, RoundingMode.FLOOR).longValueExact();
        } catch (ArithmeticException ex) {
            throw new InputException(InputException.Kind.INVALID_INPUT, field.get(), "overflow");
        }
    }

    /** 거리 전용. 소수는 거부(표기 기준, scale > 0). */
    public static int toWholeMeters(BigDecimal value, Supplier<String> field) {
        return toWholeNonNegativeInt(value, field);
    }

    /** 시간 전용. 소수는 거부(표기 기준, scale > 0). 음수도 거부. */
    public static int toWholeSeconds(BigDecimal value, Supplier<String> field) {
        return toWholeNonNegativeInt(value, field);
    }

    private static int toWholeNonNegativeInt(BigDecimal value, Supplier<String> field) {
        if (value.scale() > 0) {
            throw new InputException(InputException.Kind.INVALID_INPUT, field.get(), "fractional");
        }
        if (value.signum() < 0) {
            throw new InputException(InputException.Kind.INVALID_INPUT, field.get(), "negative");
        }
        try {
            return value.intValueExact();
        } catch (ArithmeticException ex) {
            throw new InputException(InputException.Kind.INVALID_INPUT, field.get(), "overflow");
        }
    }
}
