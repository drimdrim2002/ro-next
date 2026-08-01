# Phase 02 Handoff Contract (tracked summary)

Authority for digests remains `target/phase-01-evidence/handoff/` (sealed).
This file is a tracked pointer for Phase 02 Domain·Travel owners.

## Consume

| Item | Detail |
| :--- | :--- |
| Artifact type | `com.ronext.rpdptw.normalization.NormalizedInputArtifact` |
| Entry path | External bytes → `adapters/input` (`TEST_FIXTURE_V1` in tests) → `CanonicalBusinessInput` → `DefaultCanonicalInputNormalizer` → sealed artifact **or** ordered rejection |
| Digests | `rawInputDigest` (SHA-256), `semanticFingerprint`, `envelopeFingerprint` |
| Policy snapshot | captured on artifact; Phase 01 does not bind profile/preset defaults |

## Must preserve as-is

- External identity graph (no dense IDs in Phase 01)
- `servicePattern` + per-side `reqDate` (request-time meaning)
- customer / profile / version + preset omission
- mandatory + approved extension declarations (**unbound**)
- sparse integer travel `D`/`U` only when present
- vehicle speed **present \| absent** (do not assume 45 already filled)
- ownership **present \| absent** (do not assume DIRECT)
- multi-zone / all-zones vehicle zone sets
- trip / wait / route-resource typed absence
- `StaticUnassignabilityFact` list (e.g. zone conflict)

## Out of Phase 01 (Phase 02+ owns)

- Dense internal IDs
- `PreparedTravel` / Great Circle / speed→`U` execution
- Immutable solve snapshot assembly
- Filling absent speed with `45`
- Public production wire / multi-version schema ops

## Negative catalog

See sealed `handoff/negative-fixture-catalog.md` and
`com.ronext.rpdptw.fixture.Phase01FailureFixtures`.

## Acceptance note

Phase 01 evidence is **produced pending independent review**.
`phase_acceptance_status` remains **NOT_ACCEPTED** until review + user promotion.
