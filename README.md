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

| Option       | Description          |
|--------------|----------------------|
| -h, --help   | Show this message and exit |

---

### 📋 list-jdks

Lists all discovered JDK installations.

```bash
jdkcerts list-jdks [--custom-jdk-dirs <VALUE>]
```

**Options:**

| Option                     | Description                                              |
|----------------------------|----------------------------------------------------------|
| --custom-jdk-dirs <VALUE>  | Comma-separated paths to JDK directories (optional)      |
| -h, --help                 | Show this message and exit                               |

---

### 📥 install-cert

Installs a certificate across all discovered JDK keystores.

```bash
jdkcerts install-cert --cert <PATH> --alias <ALIAS> [--keystore-password <PASSWORD>] [--dry-run] [--custom-jdk-dirs <VALUE>]
```

**Options:**

| Option                     | Description                                              | Default    |
|----------------------------|----------------------------------------------------------|------------|
| --cert <VALUE>             | Path to the certificate file (**required**)              |            |
| --alias <TEXT>             | Certificate alias (**required**)                         |            |
| --keystore-password <TEXT> | Keystore password                                        | `changeit` |
| --custom-jdk-dirs <VALUE>  | Comma-separated paths to JDK directories (optional)      |            |
| --dry-run                  | Preview changes without modifying anything               |            |
| -h, --help                 | Show this message and exit                               |            |

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

| Option                     | Description                                              | Default    |
|----------------------------|----------------------------------------------------------|------------|
| --alias <TEXT>             | Certificate alias (**required**)                         |            |
| --keystore-password <TEXT> | Keystore password                                        | `changeit` |
| --custom-jdk-dirs <VALUE>  | Comma-separated paths to JDK directories (optional)      |            |
| --dry-run                  | Preview changes without modifying anything               |            |
| -h, --help                 | Show this message and exit                               |            |

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

| Option                     | Description                                                     | Default    |
|----------------------------|-----------------------------------------------------------------|------------|
| --alias <TEXT>             | Certificate alias or search pattern (**required**)              |            |
| --keystore-password <TEXT> | Keystore password                                               | `changeit` |
| --verbose                  | Display all certificate details (SHA1, SHA256, Serial, etc.)    | `false`    |
| --regex                    | Search by regex pattern instead of exact match                  |            |
| --closest-match            | Search by closest match (fuzzy matching for typos)              |            |
| --custom-jdk-dirs <VALUE>  | Comma-separated paths to JDK directories (optional)             |            |
| -h, --help                 | Show this message and exit                                      |            |

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
