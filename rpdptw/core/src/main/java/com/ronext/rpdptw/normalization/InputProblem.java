package com.ronext.rpdptw.normalization;

import java.util.Objects;

public sealed interface InputProblem {
    InputProblemCode code();
    InputPath path();

    record Schema(InputProblemCode code, InputPath path) implements InputProblem {
        public Schema {
            Objects.requireNonNull(code, "code must not be null");
            Objects.requireNonNull(path, "path must not be null");
        }
    }

    record Identity(InputProblemCode code, InputPath path) implements InputProblem {
        public Identity {
            Objects.requireNonNull(code, "code must not be null");
            Objects.requireNonNull(path, "path must not be null");
        }
    }

    record Reference(InputProblemCode code, InputPath path) implements InputProblem {
        public Reference {
            Objects.requireNonNull(code, "code must not be null");
            Objects.requireNonNull(path, "path must not be null");
        }
    }

    record Numeric(InputProblemCode code, InputPath path) implements InputProblem {
        public Numeric {
            Objects.requireNonNull(code, "code must not be null");
            Objects.requireNonNull(path, "path must not be null");
        }
    }

    record Temporal(InputProblemCode code, InputPath path) implements InputProblem {
        public Temporal {
            Objects.requireNonNull(code, "code must not be null");
            Objects.requireNonNull(path, "path must not be null");
        }
    }

    record Compatibility(InputProblemCode code, InputPath path) implements InputProblem {
        public Compatibility {
            Objects.requireNonNull(code, "code must not be null");
            Objects.requireNonNull(path, "path must not be null");
        }
    }

    record Trip(InputProblemCode code, InputPath path) implements InputProblem {
        public Trip {
            Objects.requireNonNull(code, "code must not be null");
            Objects.requireNonNull(path, "path must not be null");
        }
    }
}
