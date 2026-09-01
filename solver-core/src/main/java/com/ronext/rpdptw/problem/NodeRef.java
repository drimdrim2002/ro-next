package com.ronext.rpdptw.problem;

import java.util.Objects;

import com.ronext.rpdptw.domain.RequestId;
import com.ronext.rpdptw.domain.RequestSide;

public record NodeRef(RequestId requestId, boolean pickup, RequestSide side) {

    public NodeRef {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(side, "side");
    }
}
