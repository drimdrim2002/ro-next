package com.ronext.rpdptw.adapter.input;

import com.ronext.rpdptw.input.AdapterIdentity;

public interface InputAdapter {
    boolean supports(AdapterIdentity adapter);
    AdaptationResult adapt(ExternalInputDocument document);
}
