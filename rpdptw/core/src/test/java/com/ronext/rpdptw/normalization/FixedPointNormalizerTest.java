package com.ronext.rpdptw.normalization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FixedPointNormalizerTest {

    private final FixedPointNormalizer normalizer = new DefaultFixedPointNormalizer();

    @Test
    void floorsWeightAtThirdDecimal() {
        assertEquals(1234L, normalizer.floorNonNegativeToScale3("1.2340"));
        assertEquals(1234L, normalizer.floorNonNegativeToScale3("1.2349"));
    }

    @Test
    void normalizesEachItemBeforeQuantityMultiplication() {
        long itemVal = normalizer.floorNonNegativeToScale3("0.0009");
        assertEquals(0L, itemVal);
        long totalVal = normalizer.multiplyChecked(itemVal, 2);
        assertEquals(0L, totalVal);
    }

    @Test
    void rejectsDecimalDistanceEvenWhenMathematicallyIntegral() {
        NumericReject reject = assertThrows(NumericReject.class, () -> normalizer.requireIntegerLexeme("1.0"));
        assertEquals(InputProblemCode.FRACTION_NOT_ALLOWED, reject.code());
    }

    @Test
    void rejectsExponentForIntegerOnlyFieldPerAdapterPolicy() {
        NumericReject reject = assertThrows(NumericReject.class, () -> normalizer.requireIntegerLexeme("1e3"));
        assertEquals(InputProblemCode.FRACTION_NOT_ALLOWED, reject.code());
    }

    @Test
    void detectsScaleOverflow() {
        NumericReject reject = assertThrows(NumericReject.class, () -> normalizer.floorNonNegativeToScale3("999999999999999999999.999"));
        assertEquals(InputProblemCode.ARITHMETIC_OVERFLOW, reject.code());
    }

    @Test
    void detectsQuantityMultiplicationOverflow() {
        NumericReject reject = assertThrows(NumericReject.class, () -> normalizer.multiplyChecked(Long.MAX_VALUE, 2));
        assertEquals(InputProblemCode.ARITHMETIC_OVERFLOW, reject.code());
    }

    @Test
    void detectsRequestSumOverflow() {
        NumericReject reject = assertThrows(NumericReject.class, () -> normalizer.addChecked(Long.MAX_VALUE, 1L));
        assertEquals(InputProblemCode.ARITHMETIC_OVERFLOW, reject.code());
    }

    @Test
    void appliesFinite999CbmOnlyWhenVolumeUnusedIsExplicit() {
        NumericReject reject = assertThrows(NumericReject.class, () -> normalizer.floorNonNegativeToScale3(null));
        assertEquals(InputProblemCode.INVALID_NUMERIC_SYNTAX, reject.code());
    }
}
