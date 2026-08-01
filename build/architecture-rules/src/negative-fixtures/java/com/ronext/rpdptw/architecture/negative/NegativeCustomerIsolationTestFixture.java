package com.ronext.rpdptw.architecture.negative;

public class NegativeCustomerIsolationTestFixture {
    public boolean isAcmeCustomer(String customerId) {
        return customerId.equals("acmeevil");
    }
}
