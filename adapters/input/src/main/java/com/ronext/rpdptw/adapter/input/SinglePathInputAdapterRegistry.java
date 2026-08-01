package com.ronext.rpdptw.adapter.input;

import com.ronext.rpdptw.adapter.input.testfixture.TestFixtureInputAdapter;
import com.ronext.rpdptw.normalization.InputPath;
import com.ronext.rpdptw.normalization.InputProblem;
import com.ronext.rpdptw.normalization.InputProblemCode;
import com.ronext.rpdptw.normalization.InputRejectionReport;

import java.util.List;
import java.util.Objects;

public final class SinglePathInputAdapterRegistry {

    private final List<InputAdapter> adapters;

    public SinglePathInputAdapterRegistry(List<InputAdapter> adapters) {
        Objects.requireNonNull(adapters, "adapters must not be null");
        this.adapters = List.copyOf(adapters);
    }

    public AdaptationResult adapt(ExternalInputDocument document) {
        Objects.requireNonNull(document, "document must not be null");
        InputAdapter match = null;
        for (InputAdapter adapter : adapters) {
            if (adapter.supports(document.declaredAdapterIdentity())) {
                if (match != null) {
                    throw new IllegalStateException("Multiple adapters found matching identity: " + document.declaredAdapterIdentity());
                }
                match = adapter;
            }
        }

        if (match == null) {
            return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                    new InputProblem.Schema(
                            InputProblemCode.UNSUPPORTED_ADAPTER_OR_SOURCE,
                            new InputPath("adapter")
                    )
            )));
        }

        return match.adapt(document);
    }

    public static SinglePathInputAdapterRegistry testDefault() {
        return new SinglePathInputAdapterRegistry(List.of(new TestFixtureInputAdapter()));
    }
}
