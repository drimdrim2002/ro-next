package com.ronext.rpdptw.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ronext.rpdptw.eval.DefaultProfile;
import com.ronext.rpdptw.eval.Evaluation;
import com.ronext.rpdptw.eval.HardConstraint;
import com.ronext.rpdptw.eval.Profile;
import com.ronext.rpdptw.eval.RouteFacts;
import com.ronext.rpdptw.problem.Problem;

class ProfileRegistryTest {

    @Test
    void unregisteredResolvesToDefault() {
        DefaultProfile defaults = new DefaultProfile();
        Profile registered = new NamedProfile("acme");
        ProfileRegistry registry = new ProfileRegistry(Map.of("acme", registered), defaults);

        assertSame(defaults, registry.resolve(Optional.of("unknown")));
        assertSame(defaults, registry.resolve(Optional.empty()));
        assertSame(registered, registry.resolve(Optional.of("acme")));

        ProfileRegistry builtIn = ProfileRegistry.builtIn();
        assertEquals(DefaultProfile.ID, builtIn.resolve(Optional.of("anyone")).id());
        assertEquals(DefaultProfile.ID, builtIn.resolve(Optional.empty()).id());
        assertEquals(DefaultProfile.ID, builtIn.defaultProfile().id());
    }

    private static final class NamedProfile implements Profile {
        private final String id;

        private NamedProfile(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public List<HardConstraint> hardConstraints() {
            return List.of();
        }

        @Override
        public long[] score(Problem problem, Evaluation metrics, Collection<RouteFacts> routes) {
            return new long[] {metrics.unassignedCount()};
        }
    }
}
