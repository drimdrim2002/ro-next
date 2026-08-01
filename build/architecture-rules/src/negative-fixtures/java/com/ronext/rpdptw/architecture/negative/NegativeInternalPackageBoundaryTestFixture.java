package com.ronext.rpdptw.architecture.negative;

import com.ronext.rpdptw.solver.internal.SecretSolver;

public class NegativeInternalPackageBoundaryTestFixture {
    public void accessInternalPackage() {
        SecretSolver.doSecret();
    }
}
