# 🚀 CI/CD Workflows Overview

GitHub Actions workflows used to build, test, lint, secure, and release the **jdkcerts** tool.

---

# 📦 Release Pipeline ([release.yml](release.yml))

Automates the full release process—from version bumping to Homebrew formula updates.

## 🎯 Trigger

Manual run via **GitHub Actions → Run workflow**, with one required input: `tag` (e.g., `v1.2.0`)

## 🔄 Pipeline Summary

1. **Version Bump**  
   Updates `version` in `build.gradle.kts` and commits the change.

2. **Build**  
   Sets up **JDK 21** and builds the fat JAR using `./gradlew shadowJar`.

3. **GitHub Release**

   Creates a new formal release on GitHub and uploads the versioned artifact:
    - JDKCertsTool-vX.Y.Z.jar

4. **Tap Synchronization (Homebrew)**

   Triggers a repository_dispatch event to the [ADarko22/homebrew-tap](https://github.com/ADarko22/homebrew-tap)
   repository. This event passes the version and artifact name, prompting the Tap to:
    - Download the new JAR.
    - Calculate the new SHA256 checksum.
    - Update the jdkcerts.rb formula.

---

# 🧪 Build & Test Workflow ([build-and-test.yml](build-and-test.yml))

Ensures every commit and PR builds cleanly, passes tests, and produces coverage reports.

## 🎯 Trigger

- `push` to `master`
- `pull_request` → `master`

## 🔄 Pipeline Summary

1. Build & Test
    - Checkout code, set up **JDK 17** and Gradle
    - Run `clean build`, `test`, and `jacocoTestReport`

2. Artifacts (always uploaded)
    - JUnit XML + HTML test reports
    - JaCoCo coverage report

3. Test Summary
    - Published via `dorny/test-reporter@v1`

---

# 🔍 PR Linting & Security ([pull_request_checks.yml](pull_request_checks.yml))

Provides linting and CodeQL security analysis on pull requests.

## 🎯 Trigger

- `pull_request` → `master`

## 🔄 Pipeline Summary

1. Linting (Super-Linter)
    - Validates Kotlin changed files
    - Adds comments and status updates to the PR.

2. CodeQL Analysis
    - Sets up JDK 17 & Gradle
    - Initializes CodeQL for Kotlin
    - Builds the project
    - Uploads security findings
