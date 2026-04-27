---
description: "Use when creating or editing Java code in Workastra Platform modules (core, console, iam, migration). Enforces Java 25 + Spring Boot 4 conventions, Checkstyle constraints, and NullAway/JSpecify nullness rules."
applyTo: "**/*.java"
---

# Workastra Java Conventions

- Target Java 25 language features that are already used in the codebase.
- Keep package names under `com.workastra`.
- Use 4 spaces for indentation; do not use tab characters.
- Avoid wildcard imports. The only allowed static wildcard import is `org.assertj.core.api.Assertions.*` in tests.
- Prefer constructor injection and immutable dependencies (`private final` fields).
- Use `this.` for instance member access.

## Nullness and Package Setup

- Default to non-null types.
- Mark nullable references explicitly with `org.jspecify.annotations.Nullable`.
- Keep each Java package `@NullMarked` via a `package-info.java` file.
- When adding a new package under `com.workastra`, add a matching `package-info.java` with `@NullMarked`.

## Spring Configuration Rules

- `@Bean` methods must be package-private (no `public`, `protected`, or `private` modifier).
- Keep Spring Boot application entry points package-private (`static void main(String[] args)`).

## Verification Before Finishing

- Run module-focused checks for touched code, for example:
  - `./gradlew :iam:check`
  - `./gradlew :console:check`
  - `./gradlew :core:check`
  - `./gradlew :migration:check`
- If changes span multiple modules, run `./gradlew check` from the repository root.
