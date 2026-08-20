# Architectural Testing

Architectural constraints are automatically verified at build time using **[ArchUnit](https://www.archunit.org/)** with
JUnit.

## Verification Scope

Tests under `src/test/kotlin/edu/adarko22/jdkcerts/architecture` enforce the desired architecture from the code
structure point of view (i.e. packaging structure, dependency direction, class and methods conformance, etc.).

## Frozen Baseline

Existing violations are recorded using [
`FreezingArchRule.freeze()`](https://www.archunit.org/userguide/html/000_Index.html#_freezing_arch_rules). The build
permits the existing baseline but fails
on any **new** violation.

The ArchUnit configuration lives in the `archunit.properties` file inside the Test Resources Root folder.
