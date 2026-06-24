# 🚀 CI/CD Workflows Overview

GitHub Actions workflows used to build, test, lint, secure, and release the **jdkcerts** tool.

---

# 📦 Release Pipeline ([release.yml](release.yml))

Automates the full release process—from version bumping to Homebrew formula updates.

## 🎯 Trigger

Manual run via **GitHub Actions → Run workflow**, with one required input: `tag` (e.g., `v1.2.0`)

## 🔄 Pipeline Summary

1. **Calculate & Validate**

   Ensures the tag follows Semantic Versioning (X.Y.Z) and calculates the next -SNAPSHOT version.

2. **Build**

   Sets up JDK 21 and builds the fat JAR using `./gradlew shadowJar`.

3. **GitHub Release**

   Creates a formal GitHub release and uploads `JDKCertsTool-vX.Y.Z.jar`.

4. **Tap Synchronization (Homebrew)**

   Triggers a repository_dispatch to [ADarko22/homebrew-tap](https://github.com/ADarko22/homebrew-tap) , prompting the
   tap to update the `jdkcerts` formula with the new version and SHA256.

5. **Post-Release Version Bump**

   Updates projectVersion in `gradle.properties` to the next snapshot version and pushes directly to main.

## 🔐 Required SecretsTo

The pipeline requires and uses the following secrets:

| Name                 | Purpose                                             | Required For                                                                                      |
|:---------------------|:----------------------------------------------------|:--------------------------------------------------------------------------------------------------|
| `PUSH_TO_MAIN_TOKEN` | Fine-grained PAT with Contents: `Read/WriteRelease` | Pipeline (bypass branch protection)                                                               |
| `TAP_GITHUB_TOKEN`   | PAT for cross-repo events                           | Homebrew Tap synchronization to [ADarko22/homebrew-tap](https://github.com/ADarko22/homebrew-tap) |

---

# 🧪 Build, Test & Analyse Workflow ([build-test-analyse.yml](build-test-analyse.yml))

Ensures every commit and PR builds cleanly, passes tests, and produces coverage reports.

## 🎯 Trigger

- `push` to `master`
- `pull_request` → `master`

## 🔄 Pipeline Summary

1. **Linting (Super-Linter)**
    - Validates Kotlin changed files
    - Adds comments and status updates to the PR.

2. **Build Test & Analysis**
    - Sets up JDK 17 & Gradle
    - Initializes CodeQL for Kotlin
    - Builds the project & runs Sonar analysis
    - Uploads security findings

3. **Artifacts (always uploaded)**
    - JUnit XML + HTML test reports
    - JaCoCo coverage report

4. **Test Summary**
    - Published via `dorny/test-reporter@v1`
