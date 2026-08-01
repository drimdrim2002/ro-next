package com.ronext.rpdptw.core.negative;

public class NegativeCustomerIsolationTestFixture {
    public boolean isAcmeCustomer(String customerId) {
        return customerId.equals("acmeevil");
    }
}
