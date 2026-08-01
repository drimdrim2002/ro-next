/**
 * Single-canonical anti-corruption boundary (D1) for external input documents.
 * <p>
 * This package and its subpackages are responsible for translating external DTOs,
 * schemas, and wire payloads into canonical domain representations.
 * Core domain logic must never depend on classes defined in this package.
 * </p>
 * <p>
 * Forbidden dependencies in production code: solver, verification, cloud SDKs.
 * </p>
 */
package com.ronext.rpdptw.adapter.input;
