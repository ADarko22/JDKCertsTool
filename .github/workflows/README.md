# 🚀 CI/CD Workflows Overview

GitHub Actions workflows used to build, test, lint, secure, and release the **jdkcerts** tool.

---

# 📦 Release Pipeline ([release.yml](release.yml))

Automates the full release process—building native executables for each supported platform, publishing a GitHub
release, and updating the Homebrew formula.

## 🎯 Trigger

Manual run via **GitHub Actions → Run workflow**, with one required input: `tag` (e.g., `v1.2.0`)

## 🔄 Pipeline Summary

1. **prepare**
    - Checks out `master` (releases always build from `master`)
    - Validates the `tag` input and fails if a tag by that name already exists
    - Computes the clean release version and the next `-SNAPSHOT` version, exposed as job outputs

2. **build** (matrix job, _depends on prepare_)
    - Runs once per target platform: macOS arm64, macOS x86_64/Intel, Linux x86_64.
        - Sets up GraalVM 25 and builds a native executable with `./gradlew nativeCompile`
        - Packages it into a `JDKCertsTool-<version>-<os>-<arch>.tar.gz` archive
        - Uploads it as a build artifact.
    - The Linux x86_64 leg (built on `ubuntu-22.04`/glibc 2.35, one LTS behind `ubuntu-latest`, so it also runs on
      the previous LTS rather than only the very latest) additionally:
        - Builds the fat jar (`./gradlew shadowJar`)
        - Uploads the fat jar separately, as a platform-independent artifact.

3. **publish (_depends on prepare and build_)**
    - Downloads all platform artifacts (native `.tar.gz` archives + the fat jar).
    - Creates a formal GitHub release and attaches all of them.
    - Runs [`Justintime50/homebrew-releaser`](https://github.com/Justintime50/homebrew-releaser), which generates a
      `brew audit`-compliant formula, and pushes it
      directly to [ADarko22/homebrew-tap](https://github.com/ADarko22/homebrew-tap)'s `Formula/jdkcerts.rb`.
    - Updates `projectVersion` in `gradle.properties` to the next snapshot version and pushes directly to master
      (a no-op, not a failure, if that bump was already applied by a prior run of the same release).

Note the brew formula file is auto-generated on every release, and it should not be hand-edited in the tap repo.
The fat jar is **not** installed via Homebrew; it is a manual-download alternative for users who already have a Java 25+
runtime (see the installation docs).

## 🔐 Required Secrets

The pipeline requires and uses the following secrets:

| Name                 | Purpose                                             | Required For                                                                                                           |
|:---------------------|:----------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------|
| `PUSH_TO_MAIN_TOKEN` | Fine-grained PAT with Contents: `Read/WriteRelease` | Pipeline (bypass branch protection)                                                                                    |
| `TAP_GITHUB_TOKEN`   | PAT with `repo` scope on this repo and the tap      | `homebrew-releaser` pushing the generated formula to [ADarko22/homebrew-tap](https://github.com/ADarko22/homebrew-tap) |

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
    - Sets up JDK 25 & Gradle
    - Initializes CodeQL for Kotlin
    - Builds the project & runs Sonar analysis
    - Uploads security findings

3. **Artifacts (always uploaded)**
    - JUnit XML + HTML test reports
    - JaCoCo coverage report

4. **Test Summary**
    - Published via `dorny/test-reporter@v3`

5. **Native Build Smoke Test**
    - Sets up GraalVM 25 and runs `./gradlew nativeCompile`
    - Smoke-tests the resulting binary (`info`, `--help`, `list-jdks`) on every push/PR, catching native-image
      reachability regressions before they reach a release
