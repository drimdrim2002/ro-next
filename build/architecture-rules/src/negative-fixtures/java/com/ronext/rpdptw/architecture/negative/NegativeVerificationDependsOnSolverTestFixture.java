package com.ronext.rpdptw.architecture.negative;

import com.ronext.rpdptw.solver.internal.SecretSolver;

public class NegativeVerificationDependsOnSolverTestFixture {
    public void accessSolver() {
        SecretSolver.doSecret();
    }
}
