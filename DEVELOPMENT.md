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

### Clean Architecture

This project follows the Clean Architecture principles. Please have a look at the [architecture](ARCHITECTURE.md)
section for a quick overview of the project structure and principles to follow.

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

We verify the cryptographic integrity of all external artifacts using pure offline SHA-256 checksums.
This is a security gate that esnures downloaded binaries match what we expect without relying on public PGP key servers.

If you add or upgrade a library, the build will fail in CI until you refresh the local verification rules:

```bash
./gradlew clean build --write-verification-metadata sha256
```

More details
at [Dependency Verification | Gradle](https://docs.gradle.org/current/userguide/dependency_verification.html).

> ⚠️ **Note:** Always review the changes generated in `gradle/verification-metadata.xml` before committing them.

---

## 🧪 Run from Source

### Run with Gradle

```bash
./gradlew run --args="--help"
```

### Run from IntelliJ

You can use the provided run configurations in [runConfigurations](.idea/runConfigurations)`.idea/runConfigurations` to
execute the tool directly from IntelliJ:

- `Info`
- `List JDKs`
- `Install JDK Cert`
- `Remove JDK Cert`
- `Find JDK Cert`
- `Find JDK Cert with Closest Match`
- `Find JDK Cert with RegEx`

**Note**: you may need to edit the "Program arguments"  to replace placeholders like `<ALIAS>` and `<CERT_PATH>` with
actual values, and to remove `--dry-run` for permanent changes.