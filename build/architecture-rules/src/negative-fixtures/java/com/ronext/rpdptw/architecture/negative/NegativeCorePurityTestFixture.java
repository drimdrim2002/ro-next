package com.ronext.rpdptw.architecture.negative;

public class NegativeCorePurityTestFixture {
    public String readEnv() {
        return System.getenv("FOO");
    }
}
