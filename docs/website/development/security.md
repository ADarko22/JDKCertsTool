# Build Supply Chain Security

To keep dependencies safe, this project enforces strict dependency containment rules. If you introduce or upgrade a
library inside `build.gradle.kts`, you **must** update the security lock files before opening a Pull Request.

### 1. Dependency Lock Tracking

Every compile-time and transitive dependency is locked to prevent silent upgrades. Update the lockfile records by
running:

```bash
./gradlew dependencies --write-locks
```

### 2. Checksum/Signature Attestation

Artifact files are validated using offline SHA-256 signatures to block upstream tampering attempts. If libraries change,
update the cryptographic manifest metadata file:

```bash
./gradlew clean build --write-verification-metadata sha256
```

!!! warning "Review Verification Changes"
    Always carefully audit the auto-generated diff profiles inside `gradle/verification-metadata.xml` before packaging them
    inside your repository commits.

!!! note "Dependabot is reconciled automatically"
    Dependabot cannot update the verification manifest, so the `dependabot-gradle-verification.yml`
    workflow regenerates `gradle/verification-metadata.xml` and commits it back to each Dependabot Gradle
    PR. The manual command above is only needed for dependency changes you make yourself.
