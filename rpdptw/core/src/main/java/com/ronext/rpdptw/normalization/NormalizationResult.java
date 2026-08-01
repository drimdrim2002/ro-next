package com.ronext.rpdptw.normalization;

import java.util.Objects;

public sealed interface NormalizationResult {
    record Accepted(NormalizedInputArtifact artifact) implements NormalizationResult {
        public Accepted {
            Objects.requireNonNull(artifact, "artifact must not be null");
        }
    }

    record Rejected(InputRejectionReport report) implements NormalizationResult {
        public Rejected {
            Objects.requireNonNull(report, "report must not be null");
        }
    }
}
