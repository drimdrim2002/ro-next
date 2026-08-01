# ADR: Phase 00 Build Pins and Legacy Dependency Policy

## Metadata
- **Task ID**: `TASK-P00-ARCH-SKELETON`
- **Owner / Implementer**: AGY Antigravity
- **Reviewer**: Independent Phase Reviewer
- **Date**: 2026-08-01
- **Status**: Approved

---

## Context & Objectives
Phase 00 establishes the core architectural skeleton, build configuration, dependency rules, and isolation policies between target clean-slate modules (`com.ronext.rpdptw`) and legacy code (`com.ronext.legacy`). To ensure build reproducibility, dependency hygiene, and structural isolation, strict pins and enforcer policies are recorded in this ADR.

---

## Policy Decisions

### 1. Target Modules (`com.ronext.rpdptw`)
- **Enforcer Rules**: Enforce strict `dependencyConvergence` and upper-bound dependency policies across target modules.
- **Banned Dependencies**: Absolutely zero direct or indirect dependencies on prohibited cloud SDKs, heavy frameworks, or runtime infrastructure databases/caches. Specifically banned dependency groups:
  - Google Cloud / GCP SDKs (`com.google.cloud`, `com.google.apis`, etc.)
  - AWS SDKs (`software.amazon.awssdk`, `com.amazonaws`)
  - Azure SDKs (`com.azure`)
  - Google OR-Tools (`com.google.ortools`)
  - JDBC driver libraries (`org.postgresql`, `com.mysql`, `org.h2`, `oracle`, etc.)
  - Redis clients (`redis.clients`, `io.lettuce`, `org.redisson`)

### 2. Legacy Module (`com.ronext.legacy`)
- **Inventory Maintenance**: May retain current Google Storage, Google Workflow, and Jackson library coordinates strictly as isolated legacy inventory.
- **No Silent Upgrades**: No unauthorized or silent version upgrades (e.g. attempting to "fix" Jackson or dependency versions) without full golden re-proof testing.

### 3. Shade Collisions & Resource Packaging
- **Collision Policy**: Shade collisions must be explicitly inventoried and selected service resources preserved as golden reference artifacts.
- **No Broad Exclusions**: Broad or wildcard dependency/resource exclusions are forbidden without explicit architectural review.

### 4. Enforcer Execution Policy
- **No Skipping**: Setting `enforcer.skip=true` on the root default Maven lifecycle is strictly prohibited under any circumstances during standard builds and CI execution.

---

## Build Pin Table

| Component / Tool / Plugin | Version / Coordinates | Details / Hash / Policy |
| :--- | :--- | :--- |
| **Maven Wrapper** | 3.9.14 | Apache release SHA-256 (`635b719468971f11f43501a33758b9f1d8ef3df1a12edcd5e49ef2db4ef88e7b` or official SHA-256 for 3.9.14 wrapper jar/dist if downloaded) |
| `maven.compiler.release` | `25` | Java 25 bytecode target |
| **Maven Enforcer Plugin** | `3.6.1` | Enforces `dependencyConvergence` and upper-bound deps policy |
| **Maven Compiler Plugin** | `3.14.1` | Standard compiler plugin pin |
| **Maven Surefire Plugin** | `3.5.4` | Test execution runner pin |
| **Maven Shade Plugin** | `3.6.1` | Executable shadow jar build pin |
| **ArchUnit** | `com.tngtech.archunit:archunit-junit5:1.4.1` | Architectural validation framework |
| `project.build.outputTimestamp` | `2026-08-01T00:00:00Z` | Reproducible build timestamp pin |
| **JUnit** | `5.13.1` | `org.junit.jupiter:junit-jupiter` testing framework |

---

## Status & Authorization
This ADR is formally approved and recorded under `TASK-P00-ARCH-SKELETON` by AGY Antigravity and reviewed by the Independent Phase Reviewer.
