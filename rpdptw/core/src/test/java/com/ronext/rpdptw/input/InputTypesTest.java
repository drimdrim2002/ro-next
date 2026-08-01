package com.ronext.rpdptw.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputTypesTest {

    @Test
    void testAdapterIdentityValidation() {
        assertThrows(NullPointerException.class, () -> new AdapterIdentity(null));
        assertThrows(IllegalArgumentException.class, () -> new AdapterIdentity(""));
        AdapterIdentity identity = new AdapterIdentity("adapter-1");
        assertEquals("adapter-1", identity.value());
    }

    @Test
    void testSchemaIdentityValidation() {
        assertThrows(NullPointerException.class, () -> new SchemaIdentity(null));
        assertThrows(IllegalArgumentException.class, () -> new SchemaIdentity(""));
        SchemaIdentity identity = new SchemaIdentity("schema-v1");
        assertEquals("schema-v1", identity.value());
    }

    @Test
    void testExternalIdsNonNullValidation() {
        assertThrows(NullPointerException.class, () -> new ExternalPlanId(null));
        assertThrows(NullPointerException.class, () -> new ExternalRequestId(null));
        assertThrows(NullPointerException.class, () -> new ExternalVehicleId(null));
        assertThrows(NullPointerException.class, () -> new ExternalLocationId(null));

        assertEquals("p1", new ExternalPlanId("p1").value());
        assertEquals("r1", new ExternalRequestId("r1").value());
        assertEquals("v1", new ExternalVehicleId("v1").value());
        assertEquals("l1", new ExternalLocationId("l1").value());
    }

    @Test
    void testServicePatternValues() {
        assertEquals(2, ServicePattern.values().length);
        assertEquals(ServicePattern.DELIVERY_ONLY, ServicePattern.valueOf("DELIVERY_ONLY"));
        assertEquals(ServicePattern.PICKUP_DELIVERY, ServicePattern.valueOf("PICKUP_DELIVERY"));
    }

    @Test
    void testRawInputDigestDefensiveCopy() {
        byte[] input = new byte[]{1, 2, 3, 4};
        RawInputDigest digest = new RawInputDigest(input);

        // Mutate original array
        input[0] = 99;
        assertArrayEquals(new byte[]{1, 2, 3, 4}, digest.sha256());

        // Mutate returned array
        byte[] retrieved = digest.sha256();
        assertNotSame(retrieved, digest.sha256());
        retrieved[0] = 88;
        assertArrayEquals(new byte[]{1, 2, 3, 4}, digest.sha256());

        // Null check
        assertThrows(NullPointerException.class, () -> new RawInputDigest(null));
    }
}
