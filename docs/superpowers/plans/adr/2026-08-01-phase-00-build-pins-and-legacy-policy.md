# ADR: Phase 00 Build Pins and Legacy Removal Policy

## Metadata

- **Owner / Implementer**: Phase 00 implementation
- **Reviewer**: Independent Phase Reviewer (evidence-bound)
- **Date**: 2026-08-01
- **Status**: Amended — legacy module removed by user decision

---

## Context & Objectives

Phase 00 establishes the core architectural skeleton, build configuration, and
dependency isolation for target modules (`com.ronext.rpdptw`). The previous GCP
placeholder path is **not** retained as a Maven module.

---

## Policy Decisions

### 1. Target Modules (`com.ronext.rpdptw`)

- **Enforcer Rules**: `dependencyConvergence` and bannedDependencies on the
  `rpdptw` parent for all stable target modules.
- **Banned Dependencies** (non-exhaustive; enforcer + ArchUnit + bytecode gates):
  - Google Cloud / GCP SDKs (`com.google.cloud`, …)
  - AWS SDKs outside future `adapters/*` (`software.amazon.awssdk`, `com.amazonaws`)
  - Azure SDKs (`com.azure`)
  - Google OR-Tools (`com.google.ortools`) — C-17 GATED; backends only when approved
  - Redis clients (`redis.clients`, `io.lettuce`, `org.redisson`)
  - JPA / Spring Data stacks that imply a database
- **Storage**: S3 only; **no DB / no Redis**. JDBC drivers are not used and must
  not be introduced as production dependencies of target modules.

### 2. Legacy Module — Removed

- `legacy/`, `legacy/gcp-placeholder`, tracked `gcp/` deployment inventory, and
  the legacy-only `Dockerfile` are **deleted**.
- Phase 00 does **not** maintain golden characterization of GCP placeholder
  behavior.
- E-P00-LEGACY is a **removal attestation**, not a behavior golden bundle.
- Root `dependencyManagement` does not carry Google Cloud or Jackson versions
  solely for legacy.

### 3. Shade / packaging

- No legacy shaded application artifact remains in-repo.
- Future app packaging (Phase 08/11) owns shade/service-resource policy.

### 4. Enforcer Execution Policy

- Setting `enforcer.skip=true` on the root default Maven lifecycle is prohibited
  for standard builds and CI.

### 5. OPEN / GATED (must not be closed by Phase 00)

| ID | Status | Rule |
|---|---|---|
| C-17 | GATED | Hybrid route-selection / OR-Tools not implemented |
| O1 / D2 | OPEN | Lambda \| ECS — no product pin in modules |
| Q-BENCH-02 | OPEN | No official numeric defaults in POM/tests |

---

## Build Pin Table

| Component / Tool / Plugin | Version / Coordinates |
| :--- | :--- |
| **Maven Wrapper** | 3.9.14 (`distributionSha256Sum` in `.mvn/wrapper/maven-wrapper.properties`) |
| `maven.compiler.release` | `25` |
| **Maven Enforcer Plugin** | `3.6.1` |
| **Maven Compiler Plugin** | `3.14.1` |
| **Maven Surefire Plugin** | `3.5.4` |
| **ArchUnit** | `com.tngtech.archunit:archunit-junit5:1.4.1` |
| `project.build.outputTimestamp` | `2026-08-01T00:00:00Z` |
| **JUnit** | `5.13.1` |

---

## Status

Amended with legacy removal. Implementation acceptance still requires sealed
evidence + independent reviewer verdicts (`E-P00-BUILD`, `E-P00-ARCH`,
`E-P00-LEGACY`).
