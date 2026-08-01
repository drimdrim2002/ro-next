package com.ronext.rpdptw.core.negative;

import com.ronext.rpdptw.solver.internal.SecretSolver;

public class NegativeInternalPackageBoundaryTestFixture {
    public void accessInternalPackage() {
        SecretSolver.doSecret();
    }
}
