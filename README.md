# 🛠️ JDK Certs Tool

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A command-line utility to manage certificates in all your installed JDKs.

## ❓ Why JDKCertsTool?

Modern JDKs rely on a **trusted certificate store** to securely connect to HTTPS services. But managing these certificates—especially across multiple installed JDKs—can be a pain.

**JDKCertsTool makes it easy:**

- ✅ **Add or remove custom certificates** (like corporate or internal CA certs)
- 🔍 **List installed JDKs** 
- 🔐 Uses [keytool](https://docs.oracle.com/javase/10/tools/keytool.htm) under the hood — no need to learn its syntax

If you’ve ever hit SSL or trust errors when using Java with internal services, APIs, or behind proxies, **this tool helps you fix that in seconds.**

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

| Option                       | Description                                              | Default       |
|------------------------------|----------------------------------------------------------|---------------|
| `-h`, `--help`               | ❓ Show this message and exit                             |               |
| `--custom-jdk-dirs <VALUE>`  | 🗂️  Comma-separated paths to JDK directories (optional) |               |
| `--cert <VALUE>`             | 📄 Path to the certificate file (required)               |               |
| `--keystore-password <TEXT>` | 🔐 Keystore password                                     | `changeit`    |
| `--alias <TEXT>`             | 🏷️  Certificate alias                                   | `custom-cert` |
| `--dry-run`                  | 🛑 Preview changes without modifying anything            |               |

### 🗑️ remove-cert

**Options:**

| Option                       | Description                                              | Default       |
|------------------------------|----------------------------------------------------------|---------------|
| `-h`, `--help`               | ❓ Show this message and exit                             |               |
| `--custom-jdk-dirs <VALUE>`  | 🗂️  Comma-separated paths to JDK directories (optional) |               |
| `--keystore-password <TEXT>` | 🔐 Keystore password                                     | `changeit`    |
| `--alias <TEXT>`             | 🏷️  Certificate alias                                   | `custom-cert` |
| `--dry-run`                  | 🛑 Preview changes without modifying anything            |               |

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
