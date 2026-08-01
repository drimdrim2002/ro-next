package com.ronext.rpdptw.verification.negative;

import com.ronext.rpdptw.solver.internal.SecretSolver;

public class NegativeVerificationDependsOnSolverTestFixture {
    public void accessSolver() {
        SecretSolver.doSecret();
    }
}
