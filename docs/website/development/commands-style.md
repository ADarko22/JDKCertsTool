# Development Workflow & Formatting

### Execution Tasks

To spin up and run your working local code variants using the shadow fat-JAR setup:

```bash
./gradlew run
```

### Automated Testing & Coverage Reports

We maintain test suites backed by **JUnit 5**, **MockK**, and **JaCoCo** reporting configurations:

```bash
./gradlew test
```

* **JUnit Details:** Viewable at `build/reports/tests/test/`
* **Coverage Matrix:** Generated at `build/reports/jacoco/test/html/index.html`

### Code Style Validation

Coding conventions are strictly governed by **Ktlint**. Code reviews and builds in CI paths will actively fail if style
formatting violations exist.

```bash
# Scan style constraints
./gradlew ktlintCheck

# Correct formatting issues automatically
./gradlew ktlintFormat
```