# JDKCertsTool Development Guide

Thank you for contributing to **JDKCertsTool**!

This document outlines the development lifecycle, environment setup,
and codebase rules required to ensure seamless contributions.

## 🛠️ Stack Architecture

* **Language:** Kotlin JVM (Targeting Toolchain 25)
* **CLI Engine:** Clikt
* **Concurrency:** Kotlin Coroutines
* **Build System:** Gradle (with Kotlin DSL)
* **Native Packaging:** GraalVM Native Image (Native Build Tools Gradle plugin)
* **Code Quality:** Ktlint & SonarCloud
* **Testing:** JUnit 5, MockK, Mockito-Kotlin, and JaCoCo

---

## 🚀 Environment Setup

You do not need to manually install a matching JDK to build and test this project.
The build uses the **Foojay Toolchain Resolver** to automatically detect, download,
and provision the required Java 25 runtime isolated inside your local Gradle cache.

Simply clone the repository and execute the initial check:

```bash
git clone https://github.com/ADarko22/JDKCertsTool.git
cd JDKCertsTool
./gradlew check
```

---

## 🧬 Building the Native Executable

The `jdkcerts` binary is a GraalVM native image — a standalone executable with no JDK/JRE dependency at runtime.
Building it locally (as opposed to running from source with `./gradlew run`) requires an actual GraalVM 25 installation,
since `native-image` is a GraalVM-specific tool that the Foojay resolver's plain-JDK toolchains don't provide.

### Prerequisites

Install GraalVM 25 (Community Edition) and point `GRAALVM_HOME` at it, e.g. via [SDKMAN!](https://sdkman.io/):

```bash
sdk install java 25-graalce   # run `sdk list java` first if this identifier has changed
sdk use java 25-graalce
export GRAALVM_HOME=$(sdk home java 25-graalce)
```

### Build & Run

```bash
./gradlew nativeCompile
./build/native/nativeCompile/jdkcerts --help
```

`./gradlew run` (JVM-based) remains the fast everyday feedback loop for day-to-day development.
Use `nativeCompile` specifically to verify the actual release artifact before it ships.

### Diagnosing native-image reachability issues

If `nativeCompile` or the built binary fails with a `MissingReflectionRegistrationError` (usually after adding a new
dependency), run the CLI under the native-image tracing agent to see what it touches:

```bash
./gradlew -Pagent run --args="<subcommand>"
```

Most third-party libraries' reflection/JNI needs (e.g. Mordant's JNA-based terminal detection, pulled by Clikt)
are already covered by
the [GraalVM Reachability Metadata Repository](https://github.com/oracle/graalvm-reachability-metadata),
enabled and pinned in `build.gradle.kts` (`graalvmNative { metadataRepository { ... } }`) — check there before writing
project-specific reflect config for a class that isn't part of this project's own source.

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
This is a security gate that ensures downloaded binaries match what we expect without relying on public PGP key servers.

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