# 🚀 Getting Started

We welcome your contributions!

This guide walks you through setting up your local environment to align with our codebase standards.

### Technology Stack

* **Language:** Kotlin JVM (Targeting Toolchain 25)
* **CLI Engine:** Clikt
* **Concurrency:** Kotlin Coroutines
* **Build System:** Gradle (Kotlin DSL)
* **Native Packaging:** GraalVM Native Image (Native Build Tools Gradle plugin)

### Local Environment Setup

This project uses the **Foojay Toolchain Resolver**. You do not need to pre-install a matching local Java 25 SDK; Gradle
will automatically resolve, download, and containerize the appropriate target JDK inside its local context.

```bash
# Clone the codebase
git clone https://github.com/ADarko22/JDKCertsTool.git
cd JDKCertsTool

# Execute initial verification suite
./gradlew check
```

### Building the Native Executable

`jdkcerts` ships as a GraalVM native image — a standalone binary with no JDK/JRE dependency at runtime.
Building it locally requires an actual GraalVM 25 installation (the Foojay resolver above only provisions plain JDKs,
not GraalVM's `native-image` tool):

```bash
sdk install java 25-graalce   # via SDKMAN!; run `sdk list java` first if this identifier has changed
export GRAALVM_HOME=$(sdk home java 25-graalce)

./gradlew nativeCompile
./build/native/nativeCompile/jdkcerts --help
```

`./gradlew run` remains the fast everyday dev loop; use `nativeCompile` to verify the actual release artifact.
See [DEVELOPMENT.md](https://github.com/ADarko22/JDKCertsTool/blob/master/DEVELOPMENT.md#-diagnosing-native-image-reachability-issues)
in the repo root for how to diagnose native-image reachability errors if you hit one.

### IDE Configuration

The repository includes pre-built IDE run templates located under `.idea/runConfigurations`.

When using IntelliJ IDEA,
these actions automatically populate your context with pre-configured tasks (`Info`, `List JDKs`, `Install JDK Cert`,
etc.).
