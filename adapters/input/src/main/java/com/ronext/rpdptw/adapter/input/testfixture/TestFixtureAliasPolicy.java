package com.ronext.rpdptw.adapter.input.testfixture;

public enum TestFixtureAliasPolicy {
    STRICT,
    TEST_IGNORE;

    public static TestFixtureAliasPolicy strictReject() {
        return STRICT;
    }
}
