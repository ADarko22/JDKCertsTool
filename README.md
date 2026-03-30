# 🛠️ JDK Certs Tool

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A command-line utility to manage certificates in all the installed JDKs discovered.

## ❓ Why JDKCertsTool?

Modern JDKs rely on a **trusted certificate store** to securely connect to HTTPS services. But managing these
certificates—especially across multiple installed JDKs—can be a pain.

**JDKCertsTool makes it easy:**

- ✅ **Add or remove custom certificates** (like corporate or internal CA certs)
- 🔍 **List installed JDKs**
- 🔐 Uses [keytool](https://docs.oracle.com/javase/10/tools/keytool.htm) under the hood — no need to learn its syntax

If you’ve ever hit SSL or trust errors when using Java with internal services, APIs, or behind proxies, **this tool
helps you fix that in seconds.**

_Note that this is a developer tool which assumes usage on development environment.
This assumption translates, for example, in having the same password (or the default `changeit`) for all the keystores
and implicitly assuming the **cacerts** keystore._

---

## 📦 Installation

### ✅ Via Homebrew (macOS/Linux)

```bash
brew tap ADarko22/tap
brew install jdkcerts
```

---

## 🚀 Quick Start

Use the installed `jdkcerts` command:

```bash
jdkcerts --help
```

---

## 🧪 Run from Source

### Clone the Repository

```bash
git clone https://github.com/ADarko22/JDKCertsTool.git
cd JDKCertsTool
```

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

---

## 💡 Commands Overview

### 🔍 info

Displays basic project and environment info.

```bash
jdkcerts info
```

**Options:**

| Option     | Description                |
|------------|----------------------------|
| -h, --help | Show this message and exit |

---

### 📋 list-jdks

Lists all discovered JDK installations.

```bash
jdkcerts list-jdks [--custom-jdk-dirs <VALUE>]
```

**Options:**

| Option                    | Description                                         |
|---------------------------|-----------------------------------------------------|
| --custom-jdk-dirs <VALUE> | Comma-separated paths to JDK directories (optional) |
| -h, --help                | Show this message and exit                          |

---

### 📥 install-cert

Installs a certificate across all discovered JDK keystores.

```bash
jdkcerts install-cert --cert <PATH> --alias <ALIAS> [--keystore-password <PASSWORD>] [--dry-run] [--custom-jdk-dirs <VALUE>]
```

**Options:**

| Option                     | Description                                         | Default    |
|----------------------------|-----------------------------------------------------|------------|
| --cert <VALUE>             | Path to the certificate file (**required**)         |            |
| --alias <TEXT>             | Certificate alias (**required**)                    |            |
| --keystore-password <TEXT> | Keystore password                                   | `changeit` |
| --custom-jdk-dirs <VALUE>  | Comma-separated paths to JDK directories (optional) |            |
| --dry-run                  | Preview changes without modifying anything          |            |
| -h, --help                 | Show this message and exit                          |            |

**Example:**

```bash
# Preview installation
jdkcerts install-cert --cert /path/to/cert.pem --alias my-cert --dry-run

# Actually install with default password
jdkcerts install-cert --cert /path/to/cert.pem --alias my-cert
```

---

### 🗑️ remove-cert

Removes a certificate by alias from all discovered JDK keystores.

```bash
jdkcerts remove-cert --alias <ALIAS> [--keystore-password <PASSWORD>] [--dry-run] [--custom-jdk-dirs <VALUE>]
```

**Options:**

| Option                     | Description                                         | Default    |
|----------------------------|-----------------------------------------------------|------------|
| --alias <TEXT>             | Certificate alias (**required**)                    |            |
| --keystore-password <TEXT> | Keystore password                                   | `changeit` |
| --custom-jdk-dirs <VALUE>  | Comma-separated paths to JDK directories (optional) |            |
| --dry-run                  | Preview changes without modifying anything          |            |
| -h, --help                 | Show this message and exit                          |            |

**Example:**

```bash
# Preview removal
jdkcerts remove-cert --alias my-cert --dry-run

# Actually remove
jdkcerts remove-cert --alias my-cert
```

---

### 🔍 find-cert

Finds and displays certificate details by alias across all JDK keystores.

Supports three search strategies for maximum flexibility:

- **EXACT_MATCH** (default): Fast direct alias lookup using keytool's `-alias` option
- **REGEX**: Pattern-based matching across all keystore entries (use `--regex` flag)
- **CLOSEST_MATCH**: Fuzzy matching for approximate alias names (use `--closest-match` flag)

```bash
jdkcerts find-cert --alias <ALIAS> [--keystore-password <PASSWORD>] [--verbose] [--regex|--closest-match] [--custom-jdk-dirs <VALUE>]
```

**Options:**

| Option                     | Description                                                  | Default    |
|----------------------------|--------------------------------------------------------------|------------|
| --alias <TEXT>             | Certificate alias or search pattern (**required**)           |            |
| --keystore-password <TEXT> | Keystore password                                            | `changeit` |
| --verbose                  | Display all certificate details (SHA1, SHA256, Serial, etc.) | `false`    |
| --regex                    | Search by regex pattern instead of exact match               |            |
| --closest-match            | Search by closest match (fuzzy matching for typos)           |            |
| --custom-jdk-dirs <VALUE>  | Comma-separated paths to JDK directories (optional)          |            |
| -h, --help                 | Show this message and exit                                   |            |

**Examples:**

```bash
# Exact match (fastest, uses keytool internals)
jdkcerts find-cert --alias my-cert

# Display verbose certificate details
jdkcerts find-cert --alias my-cert --verbose

# Regex search (searches all entries)
jdkcerts find-cert --alias ".*internal.*" --regex

# Fuzzy match (best for typos)
jdkcerts find-cert --alias "star" --closest-match
```

**Note:** The `--regex` and `--closest-match` flags cannot be used together. Choose one search strategy.

The CLOSEST_MATCH strategy uses a default similarity threshold of `0.3` (see
`src/main/kotlin/edu/adarko22/jdkcerts/core/jdk/keytool/usecase/FindKeytoolCertificateUseCase.kt`). Matches with a
similarity score below this threshold are not reported. Consider making this threshold configurable in a future
release (e.g. a `--closest-match-threshold` flag).

---

## 💼 Use Cases

JDKCertsTool addresses common real-world scenarios where managing JDK truststores becomes a bottleneck. Rather than
manually fighting with `keytool` for every Java version on your machine, this tool manages the entire lifecycle of your
certificates globally.

### 1. Enterprise Setup & Provisioning

**Problem:** Corporate environments frequently use SSL inspection via internal Certificate Authorities (CAs), or
developers rely on self-signed certificates for local staging servers. If your JDKs don't trust these certificates,
IDEs, build tools (Maven/Gradle), and local applications will throw SSL connection errors.
Modern developers also commonly have multiple JDK distributions installed simultaneously, making certificate management
tedious and error-prone.

**Solution:** Use `list-jdks` to verify your environment footprint, then inject a required corporate or local
certificate into all discovered JDKs in a single operation.

```bash
jdkcerts install-cert --cert /path/to/corp-ca.pem --alias corp-ca --dry-run
 ```

### 2. Troubleshooting Trust Errors

**Problem:** The dreaded `PKIX path building failed` error appears, but you're not sure whether:

- The required certificate is missing
- The certificate has expired
- The certificate was installed under an unexpected alias

**Solution:** Use flexible search strategies to locate certificates even when you only remember part of the alias or
made a typo during installation.

```bash
jdkcerts find-cert --alias "dev-srvr" --closest-match --verbose
```

### 3. Auditing & Security Hygiene

**Problem:** Over time, developer workstations and CI/CD runners accumulate obsolete, unused, or expired certificates.
Retaining compromised or expired root CAs increases security risk and can lead to unexpected runtime failures.

**Solution:** Perform periodic audits using regex-based searches to identify related certificate groups, then remove
obsolete entries from every JDK on the system with a single command.

```bash
jdkcerts find-cert --alias ".*legacy.*" --regex
jdkcerts remove-cert --alias legacy-dev-2023
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
