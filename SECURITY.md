# Security Policy

## Supported versions

JDKCertsTool follows [Semantic Versioning](https://semver.org/). Security fixes are applied to the **latest released
minor version**. Please upgrade to the latest release before reporting an issue.

| Version   | Supported          |
|-----------|--------------------|
| Latest 2.x | :white_check_mark: |
| < 2.0     | :x:                |

## Reporting a vulnerability

**Please do not report security vulnerabilities through public GitHub issues, discussions, or pull requests.**

Instead, report them privately through GitHub's built-in advisory workflow:

- Open a private report via **[Security → Advisories → Report a vulnerability](https://github.com/ADarko22/JDKCertsTool/security/advisories/new)**.

Please include, where applicable:

- A description of the vulnerability and its impact.
- Steps to reproduce (the exact `jdkcerts` command and environment — OS, JDK versions, output of `jdkcerts info`).
- Any proof-of-concept, logs, or affected code paths.

You can expect an initial acknowledgement within a few days. Once triaged, we will work on a fix, coordinate a release,
and credit you in the advisory unless you prefer to remain anonymous.

## Scope notes

JDKCertsTool is a **developer tool** that manages the JDK **truststore** (`TrustedCertEntry` entries in `cacerts`). It
deliberately does **not** manage application **private keys** (`PrivateKeyEntry`) — see the
[README scope section](README.md#-scope-the-jdk-truststore-not-the-application-keystore). Reports about private-key
handling are therefore out of scope by design.

Because the tool assumes a development environment, it defaults to the standard `changeit` truststore password. Using it
against hardened production truststores is outside its intended use.

## Supply-chain security

This project enforces dependency locking and offline SHA-256 dependency verification, and runs SonarCloud and CodeQL on
every change. See [Build Security](DEVELOPMENT.md#-build-security) for how these gates work and how to update them.
