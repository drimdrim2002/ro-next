# Phase 02 Handoff Contract (tracked summary)

Authority for digests remains `target/phase-01-evidence/handoff/` (sealed).
This file is a tracked pointer for Phase 02 Domain·Travel owners.

## Consume

| Item | Detail |
| :--- | :--- |
| Artifact type | `com.ronext.rpdptw.normalization.NormalizedInputArtifact` |
| Entry path | External bytes → `adapters/input` (`TEST_FIXTURE_V1` in tests) → `CanonicalBusinessInput` → `DefaultCanonicalInputNormalizer` → sealed artifact **or** ordered rejection |
| Digests | `rawInputDigest` (SHA-256), `semanticFingerprint` (fp-v2 normalized meaning), `envelopeFingerprint` |
| Policy snapshot | captured on artifact; Phase 01 does not bind profile/preset defaults |

## Sealed graph (normalized facts only)

Phase 02 **MUST NOT** re-parse raw decimal strings or re-apply FLOOR.

| Entity | Sealed types |
| :--- | :--- |
| Plan | `NormalizedPlanEnvelope` + `NormalizedProfileSelectionInput` + `WorkArcPolicy` + global resource Optionals |
| Vehicles | `NormalizedVehicle`: size, caps, `VehicleZoneSet`, **`VehicleOwnership`**, **`VehicleSpeedInput`**, `TripPolicy`, wait/resource Optionals |
| Requests | `NormalizedRequest`: `ServicePattern`, visits with `NormalizedWindow` + optional `reqDateFromPlanOrigin`, **`NormalizedItem` milli products**, totals |
| Travel | `NormalizedTravelArc`: integer `Seconds` / `Meters` |
| Facts | `StaticUnassignabilityFact` + `UnassignabilityReason` (incl. zone-union Option A) |

## Explicit absences

| Field | Meaning |
| :--- | :--- |
| `VehicleSpeedInput.Absent` | Phase 01 did **not** fill 45; travel prep may apply later |
| `VehicleOwnership.Absent` | ownership axis unused — **not** silent DIRECT |
| empty `requestedPreset` | omission preserved |
| empty route resource Optionals | typed absence, no numeric sentinel |

## Out of Phase 01 (Phase 02+ owns)

- Dense internal IDs
- `PreparedTravel` / Great Circle / speed→`U` execution
- Immutable solve snapshot assembly
- Public production wire / multi-version schema ops

## Acceptance note

Phase 01 evidence is **produced pending independent review**.
`phase_acceptance_status` remains **NOT_ACCEPTED** until review + user promotion.
