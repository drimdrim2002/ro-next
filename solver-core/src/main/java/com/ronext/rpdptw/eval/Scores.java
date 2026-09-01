package com.ronext.rpdptw.eval;

import java.util.Objects;

public final class Scores {

    private Scores() {}

    /** 사전식 비교. &lt; 0 이면 a가 더 좋다. 길이가 다르면 IllegalArgumentException. */
    public static int compare(long[] a, long[] b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        if (a.length != b.length) {
            throw new IllegalArgumentException("score length " + a.length + " != " + b.length);
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] < b[i]) {
                return -1;
            }
            if (a[i] > b[i]) {
                return 1;
            }
        }
        return 0;
    }
}
