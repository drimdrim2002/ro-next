package com.ronext.rpdptw.normalization;

public interface FixedPointNormalizer {
    long floorNonNegativeToScale3(String decimalLexeme);
    long multiplyChecked(long normalizedItemValue, int quantity);
    long addChecked(long left, long right);
    long requireIntegerLexeme(String lexeme);
}
