# 🚀 Getting Started

We welcome your contributions! 

This guide walks you through setting up your local environment to align with our codebase standards.

### Technology Stack

* **Language:** Kotlin JVM (Targeting Toolchain 21)
* **CLI Engine:** Clikt
* **Concurrency:** Kotlin Coroutines
* **Build System:** Gradle (Kotlin DSL)

### Local Environment Setup

This project uses the **Foojay Toolchain Resolver**. You do not need to pre-install a matching local Java 21 SDK; Gradle
will automatically resolve, download, and containerize the appropriate target JDK inside its local context.

```bash
# Clone the codebase
git clone [https://github.com/ADarko22/JDKCertsTool.git](https://github.com/ADarko22/JDKCertsTool.git)
cd JDKCertsTool

# Execute initial verification suite
./gradlew check
```

### IDE Configuration

The repository includes pre-built IDE run templates located under `.idea/runConfigurations`.

When using IntelliJ IDEA,
these actions automatically populate your context with pre-configured tasks (`Info`, `List JDKs`, `Install JDK Cert`,
etc.).
