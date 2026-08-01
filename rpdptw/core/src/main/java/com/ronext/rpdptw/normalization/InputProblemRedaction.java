package com.ronext.rpdptw.normalization;

/**
 * Public rejection surfaces expose code + path only.
 * Raw field values, emails, addresses, and input bytes must not appear
 * on {@link InputProblem} or {@link InputRejectionReport}.
 */
public final class InputProblemRedaction {

    private InputProblemRedaction() {
    }

    /**
     * Problems are constructed with code and path only — never raw input text.
     * This method documents the policy for callers building evidence reports.
     */
    public static String publicSurface(InputProblem problem) {
        return problem.code().name() + "@" + problem.path().dotted();
    }
}
