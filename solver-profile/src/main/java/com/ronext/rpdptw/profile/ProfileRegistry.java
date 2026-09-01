package com.ronext.rpdptw.profile;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.Profile;

public final class ProfileRegistry {

    private final Map<String, Profile> byCustomerId;
    private final Profile defaultProfile;

    public ProfileRegistry(Map<String, Profile> byCustomerId, Profile defaultProfile) {
        this.byCustomerId = Map.copyOf(byCustomerId);
        this.defaultProfile = Objects.requireNonNull(defaultProfile, "defaultProfile");
    }

    public Profile resolve(Optional<String> customerId) {
        Objects.requireNonNull(customerId, "customerId");
        if (customerId.isEmpty()) {
            return defaultProfile;
        }
        return byCustomerId.getOrDefault(customerId.get(), defaultProfile);
    }

    public Profile defaultProfile() {
        return defaultProfile;
    }

    public static ProfileRegistry builtIn() {
        return new ProfileRegistry(Map.of(), new DefaultProfile());
    }
}
