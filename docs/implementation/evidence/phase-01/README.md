# Phase 01 Evidence Directory

Tracked layout notes only. The sealed, checksummed bundle lives under
`target/phase-01-evidence/` (gitignored under `target/`) and is the authority
for evidence integrity.

## Status

| Field | Value |
| :--- | :--- |
| **implementation_evidence_status** | **PRODUCED_PENDING_INDEPENDENT_REVIEW** |
| **phase_acceptance_status** | **NOT_ACCEPTED** (do not self-promote) |
| **Implementation tip (bundle)** | `206482bb96627b826ffa338d50503a7a6aed683b` |
| **Branch** | `phase-01-canonical-input` |
| **Full verify** | `./mvnw -B -ntp verify` → exit 0 (100 tests, 0 fail/err/skip) |

## Evidence keys (spec §10.2)

| Key | Contents (under `target/phase-01-evidence/`) |
| :--- | :--- |
| **E-P01-NUMERIC** | boundary table, item-first oracle, overflow report + surefire |
| **E-P01-TIME** | plan/window oracle, service/trip/resource report, full-arc handoff notes |
| **E-P01-COMPAT** | size/capability/zone report, ownership/speed absence cases |
| **E-P01-ERROR** | adapter/reference, extension, ordering, fingerprint/redaction, architecture |

Plus `manifest/`, `handoff/`, sealed `checksums.sha256` + `MANIFEST.md`.

## Seal / verify

```bash
./build/verify-evidence-bundle.sh --verify target/phase-01-evidence
# root digest of sealed checksums (regenerate after re-seal):
shasum -a 256 target/phase-01-evidence/checksums.sha256
```

Recorded root digest at collection time (2026-08-01):

```text
04154d211f13acf984c6486bf21524800e54ae9912e4d6bc5d4f68c938e0c80a  target/phase-01-evidence/checksums.sha256
```

## Phase 02 handoff contract (summary)

`NormalizedInputArtifact` provides:

- `rawInputDigest`, semantic + envelope fingerprints
- external identity graph; servicePattern + per-side reqDate
- customer/profile/version + preset omission
- mandatory + approved extension declarations (unbound)
- sparse integer D/U only when present
- speed present|absent (**no** fill 45)
- vehicle multi-zone \| all-zones; ownership present|absent
- trip/wait/resource typed absence

**Not** produced: dense IDs, PreparedTravel, solve snapshot.

Detail: `target/phase-01-evidence/handoff/NormalizedInputArtifact-contract.md`.

## Known limitations

- Public wire schema **OPEN**
- Product adapter name (O3) **OPEN** — only `TEST_FIXTURE_V1` test adapter
- Default speed 45 is Phase 02+ travel-prep policy only
- `win_poc` decimal matrix is **negative** only (not official success)
- Full-arc restart **propagation** deferred to Phase 03

## Independent review

See [REVIEW_REQUEST.md](REVIEW_REQUEST.md). Do **not** invent PASS verdicts or flip
phase YAML to `ACCEPTED` without independent review + user instruction.
