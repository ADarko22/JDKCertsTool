# Contributing to JDKCertsTool

Thanks for your interest in improving **JDKCertsTool**! Contributions of all kinds are welcome — bug reports, feature
ideas, documentation fixes, and code.

This file is the entry point. The deep-dive guides live on the documentation site:

- 🛠️ [Getting Started](https://adarko22.github.io/JDKCertsTool/website/development/getting-started/) — environment setup and daily commands.
- 🏛️ [Architecture](https://adarko22.github.io/JDKCertsTool/website/architecture/layers/) — Clean Architecture layers (`core` / `cli` / `infra`) and the rules to follow.
- 📖 [Documentation site](https://adarko22.github.io/JDKCertsTool/) — user-facing docs and command reference.

## 🐛 Reporting bugs

Open a [Bug Report](https://github.com/ADarko22/JDKCertsTool/issues/new?template=bug_report.yml) and fill in the form.
The more of the following you provide, the faster it can be triaged:

- OS and architecture, and the output of `jdkcerts info`.
- The exact command you ran and its full output (run with `--dry-run` where possible).
- What you expected vs. what happened.

> **Security issues are different** — please do **not** open a public issue. Follow the [Security Policy](SECURITY.md).

## 💡 Proposing features

Open a [Feature Request](https://github.com/ADarko22/JDKCertsTool/issues/new?template=feature_request.yml). Because the
tool has a deliberately narrow scope (the JDK **truststore**, not the application keystore — see the
[README](README.md#-scope-the-jdk-truststore-not-the-application-keystore)), it helps to describe the concrete problem
you're solving so we can confirm it fits before you invest in a PR.

## 🔀 Submitting changes

1. **Fork & branch** off `master` (e.g. `feat/windows-support`, `fix/keytool-classifier`).
2. **Follow the architecture.** New business logic goes in `core`; OS-specific code in `infra`; CLI wiring in `cli`.
   Dependencies only flow inward. See the [Architecture docs](https://adarko22.github.io/JDKCertsTool/website/architecture/layers/).
3. **Add tests** mirroring the existing patterns under `src/test/kotlin/...` and keep coverage healthy.
4. **Verify locally** before pushing:
   ```bash
   ./gradlew check        # build + tests + ktlint
   ./gradlew ktlintFormat # auto-fix style violations
   ```
5. **If you touched dependencies**, refresh the supply-chain manifests (details in
   [Build Security](https://adarko22.github.io/JDKCertsTool/website/development/security/)):
   ```bash
   ./gradlew dependencies --write-locks
   ./gradlew clean build --write-verification-metadata sha256
   ```
   > This manual refresh is for your own dependency changes. Dependabot PRs are reconciled
   > automatically by the `dependabot-gradle-verification.yml` workflow.
6. **Open a Pull Request** against `master` and fill in the PR template. CI (Super-Linter, build/test, SonarCloud,
   CodeQL) must be green before review.

## 📝 Commit messages

This project uses [Conventional Commits](https://www.conventionalcommits.org/) — `type: summary`, e.g.:

```
feat: add Windows truststore discovery
fix: classify keytool "alias already exists" as a non-fatal outcome
docs: correct license references
refactor: extract keytool failure classifier
chore: bump dependencies
```

## 📄 License

By contributing, you agree that your contributions are licensed under the [Apache License 2.0](LICENSE).
