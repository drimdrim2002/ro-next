package com.ronext.rpdptw.normalization;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DefaultFixedPointNormalizer implements FixedPointNormalizer {

    @Override
    public long floorNonNegativeToScale3(String decimalLexeme) {
        if (decimalLexeme == null) {
            throw new NumericReject(InputProblemCode.INVALID_NUMERIC_SYNTAX);
        }
        BigDecimal bd;
        try {
            bd = new BigDecimal(decimalLexeme);
        } catch (NumberFormatException | ArithmeticException e) {
            throw new NumericReject(InputProblemCode.INVALID_NUMERIC_SYNTAX);
        }

        if (bd.signum() < 0) {
            throw new NumericReject(InputProblemCode.NEGATIVE_VALUE);
        }

        try {
            return bd.movePointRight(3)
                    .setScale(0, RoundingMode.FLOOR)
                    .longValueExact();
        } catch (ArithmeticException e) {
            throw new NumericReject(InputProblemCode.ARITHMETIC_OVERFLOW);
        }
    }

    @Override
    public long multiplyChecked(long normalizedItemValue, int quantity) {
        if (quantity <= 0) {
            throw new NumericReject(InputProblemCode.NON_POSITIVE_QUANTITY);
        }
        try {
            return Math.multiplyExact(normalizedItemValue, quantity);
        } catch (ArithmeticException e) {
            throw new NumericReject(InputProblemCode.ARITHMETIC_OVERFLOW);
        }
    }

    @Override
    public long addChecked(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException e) {
            throw new NumericReject(InputProblemCode.ARITHMETIC_OVERFLOW);
        }
    }

    @Override
    public long requireIntegerLexeme(String lexeme) {
        if (lexeme == null || lexeme.isEmpty() || lexeme.contains(".") || lexeme.contains("e") || lexeme.contains("E")) {
            throw new NumericReject(InputProblemCode.FRACTION_NOT_ALLOWED);
        }
        long value;
        try {
            value = Long.parseLong(lexeme);
        } catch (NumberFormatException e) {
            throw new NumericReject(InputProblemCode.INVALID_NUMERIC_SYNTAX);
        }
        if (value < 0) {
            throw new NumericReject(InputProblemCode.NEGATIVE_VALUE);
        }
        return value;
    }
}
