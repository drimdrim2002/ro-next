# Phase 01 Evidence Directory

Tracked layout notes only. The sealed, checksummed bundle lives under
`target/phase-01-evidence/` (gitignored under `target/`) and is the authority
for evidence integrity.

## Status

| Field | Value |
| :--- | :--- |
| **implementation_evidence_status** | **PRODUCED_PENDING_INDEPENDENT_REVIEW** (post sealed-artifact remediation) |
| **phase_acceptance_status** | **NOT_ACCEPTED** (do not self-promote) |
| **Branch** | `phase-01-canonical-input` |
| **Full verify** | `./mvnw -B -ntp verify` |

## Remediation note (2026-08-01)

`NormalizedInputArtifact` now seals **Normalized\*** entity graphs (milli weights,
windows, ownership/speed sealed types, integer travel). Semantic fingerprint is
**fp-v2** over normalized meaning. §9.3 fixtures have behavioral e2e oracles.

## Evidence keys (spec §10.2)

| Key | Contents (under `target/phase-01-evidence/`) |
| :--- | :--- |
| **E-P01-NUMERIC** | boundary table, item-first oracle, overflow report + surefire |
| **E-P01-TIME** | plan/window oracle, service/trip/resource report, full-arc handoff notes |
| **E-P01-COMPAT** | size/capability/zone report, ownership/speed absence cases |
| **E-P01-ERROR** | adapter/reference, extension, ordering, fingerprint/redaction, architecture |

## Seal / verify

```bash
./build/verify-evidence-bundle.sh --verify target/phase-01-evidence
shasum -a 256 target/phase-01-evidence/checksums.sha256
```

Recorded root digest after sealed-artifact remediation:

```text
c69db0870f59467396ceb1546ef984c946f8831880ae098ec326feabb4012321  target/phase-01-evidence/checksums.sha256
```

## Independent review

See [REVIEW_REQUEST.md](REVIEW_REQUEST.md). Do **not** invent PASS verdicts or flip
phase YAML to `ACCEPTED` without independent review + user instruction.
