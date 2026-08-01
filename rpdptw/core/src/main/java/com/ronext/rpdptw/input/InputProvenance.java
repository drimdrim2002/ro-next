package com.ronext.rpdptw.input;

import java.util.List;
import java.util.Objects;

public record InputProvenance(
    AdapterIdentity adapterIdentity,
    SchemaIdentity schemaIdentity,
    List<String> aliasesApplied,
    List<String> unknownFieldsIgnored
) {
    public InputProvenance {
        Objects.requireNonNull(adapterIdentity, "adapterIdentity must not be null");
        Objects.requireNonNull(schemaIdentity, "schemaIdentity must not be null");
        Objects.requireNonNull(aliasesApplied, "aliasesApplied must not be null");
        Objects.requireNonNull(unknownFieldsIgnored, "unknownFieldsIgnored must not be null");
        aliasesApplied = List.copyOf(aliasesApplied);
        unknownFieldsIgnored = List.copyOf(unknownFieldsIgnored);
    }
}
