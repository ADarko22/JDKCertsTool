# JDKCertsTool Development Guide

Thank you for contributing to **JDKCertsTool**!

This document outlines the development lifecycle, environment setup,
and codebase rules required to ensure seamless contributions.

## 🛠️ Stack Architecture

* **Language:** Kotlin JVM (Targeting Toolchain 21)
* **CLI Engine:** Clikt
* **Concurrency:** Kotlin Coroutines
* **Build System:** Gradle (with Kotlin DSL)
* **Code Quality:** Ktlint & SonarCloud
* **Testing:** JUnit 5, MockK, Mockito-Kotlin, and JaCoCo

---

## 🚀 Environment Setup

You do not need to manually install a matching JDK to build this project.
The build uses the **Foojay Toolchain Resolver** to automatically detect, download,
and provision the required Java 21 runtime isolated inside your local Gradle cache.

Simply clone the repository and execute the initial check:

```bash
git clone https://github.com/ADarko22/JDKCertsTool.git
cd JDKCertsTool
./gradlew check

```

---

## 💻 Daily Development Commands

### Running the Application Locally

To boot up and run the CLI directly from source using the shadow fat JAR configuration:

```bash
./gradlew run
```

### Testing & Code Coverage

We enforce rigorous code verification.
Running the test suite automatically triggers **JaCoCo** to output code coverage analysis:

```bash
./gradlew test
```

* **JUnit Reports:** Located at `build/reports/tests/test/`
* **JaCoCo Coverage HTML:** Located at `build/reports/jacoco/test/html/index.html`

### Code Style & Formatting

This project uses **Ktlint** to keep the formatting uniform.
The build will fail if code style violations are discovered.

* **Check code style:** `./gradlew ktlintCheck`
* **Auto-format code:** `./gradlew ktlintFormat`

---

## 🔒 Build Security

To protect our software supply chain, this project enforces strict dependency lockdowns.
If you introduce or upgrade a library in `build.gradle.kts`,
you **must** update the security manifests locally before committing.

### 1. Dependency Locking

Every single compiled and transitive dependency version is locked.
If you modify dependencies, update your lock files by running:

```bash
./gradlew dependencies --write-locks
```

### 2. Dependency Verification (Checksums & Signatures)

We verify the integrity of all external artifacts using SHA-256 and PGP keys.
If you add a library, Gradle will fail until you refresh the verification rules:

```bash
./gradlew --write-verification-metadata pgp,sha256 --export-keys

```

> ⚠️ **Note:** This must always be run manually and reviewed by a developer to safely check and audit the modifications.
