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
brew tap ADarko22/JDKCertsTool https://github.com/ADarko22/JDKCertsTool
brew install jdkcerts
````

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

**Note**: you may need to edit the "Program arguments"  to replace placeholders like `<ALIAS>` and `<CERT_PATH>` with
actual values, and to remove `--dry-run` for permanent changes.

---

## 💡 Commands Overview

### 🔍 info

Displays basic project and environment info.

### 📋 list-jdks

**Options:**

| Option                      | Description                                              |
|-----------------------------|----------------------------------------------------------|
| `-h`, `--help`              | ❓ Show this message and exit                             |
| `--custom-jdk-dirs <VALUE>` | 🗂️  Comma-separated paths to JDK directories (optional) |

### 📥 install-cert

**Options:**

| Option                       | Description                                              | Default    |
|------------------------------|----------------------------------------------------------|------------|
| `-h`, `--help`               | ❓ Show this message and exit                             |            |
| `--custom-jdk-dirs <VALUE>`  | 🗂️  Comma-separated paths to JDK directories (optional) |            |
| `--cert <VALUE>`             | 📄 Path to the certificate file (required)               |            |
| `--keystore-password <TEXT>` | 🔐 Keystore password                                     | `changeit` |
| `--alias <TEXT>`             | 🏷️  Certificate alias                                   |            |
| `--dry-run`                  | 🛑 Preview changes without modifying anything            |            |

### 🗑️ remove-cert

**Options:**

| Option                       | Description                                              | Default    |
|------------------------------|----------------------------------------------------------|------------|
| `-h`, `--help`               | ❓ Show this message and exit                             |            |
| `--custom-jdk-dirs <VALUE>`  | 🗂️  Comma-separated paths to JDK directories (optional) |            |
| `--keystore-password <TEXT>` | 🔐 Keystore password                                     | `changeit` |
| `--alias <TEXT>`             | 🏷️  Certificate alias                                   |            |
| `--dry-run`                  | 🛑 Preview changes without modifying anything            |            |

### 🔍 find-cert

**Options:**

| Option                           | Description                                                     | Default    |
|----------------------------------|-----------------------------------------------------------------|------------|
| **`-h, --help`**                 | ❓ Show this message and exit                                    |            |
| **`--custom-jdk-dirs <VALUE>`**  | 🗂️ Comma-separated paths to JDK directories (optional)         |            |
| **`--keystore-password <TEXT>`** | 🔐 Keystore password                                            | `changeit` |
| **`--alias <TEXT>`**             | 🏷️ Certificate alias (**required**)                            |            |
| **`-v, --verbose`**              | 🔬 Display all certificate details (SHA1, SHA256, Serial, etc.) | `false`    |

---

## 💼 Use Case

JDKCertsTool is designed for real-world Java environments where trusting internal or custom certificates is crucial for
secure communication:

- **Connecting Backend Services to Internal Authentication Servers:**  
  When your Java backend calls internal SSO, OAuth2/OIDC providers, or custom token services, the JDK must trust the
  server’s certificate chain. Without the proper CA certificates imported, SSL handshakes fail, blocking authentication
  and API calls.

- **Testing OAuth2/OIDC Tokens with IntelliJ HTTP Client:**  
  Developers retrieving access tokens or testing APIs
  via [IntelliJ HTTP Client](https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html#) often face
  SSL errors if the internal auth servers use certificates not trusted by the default JDK certificate keystore. Adding
  these certs avoids frustrating connection failures during development.

- **Accessing Internal Maven or Gradle Repositories:**  
  Private artifact repositories often use self-signed or corporate CA certificates. When the JDK doesn’t trust these,
  builds fail with SSL errors. Installing the correct certificates ensures smooth dependency resolution in CI and local
  builds.

More in general, **resolving `PKIX path building failed` SSL Errors**.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
