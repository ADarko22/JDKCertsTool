# 🛠️ JDK Certs Tool

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A command-line utility to manage certificates in all the installed JDKs discovered.
Visitthe [JDKCertsTool official page](https://adarko22.github.io/JDKCertsTool/website) to discover more about!

## ❓ Why JDKCertsTool?

Modern JDKs rely on a **trusted certificate store** to securely connect to HTTPS services. But managing these
certificates—especially across multiple installed JDKs—can be a pain.

**JDKCertsTool makes devs life easy!**

- ✅ **Add or remove custom certificates** (like corporate or internal CA certs).
- 🔍 **Search Certificates** by _exact-match, fuzzy-match or regex_ on alias.
- 🔍 **Discover installed JDKs** automatically, or limit scopes to explicitly isolated paths.
- 🔐 Uses [keytool](https://docs.oracle.com/javase/10/tools/keytool.htm) under the hood — no need to learn its syntax.
- ⚡ Executes keytool operations in parallel across discovered JDKs for better throughput.

If you’ve ever hit SSL or trust errors when using Java with internal services, APIs, or behind proxies, **this tool
helps you fix that in seconds.**

_Note that this is a developer tool which assumes usage on development environment.
This assumption translates, for example, in having the same password (or the default `changeit`) for all the keystores
and implicitly assuming the **cacerts** keystore._

---

## 📦 Installation

### Via Homebrew (macOS/Linux)

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

### 💡 Commands Overview

Checkout the [Commands Overview](USAGE.md) section to learn about the commands, their options and examples.

---

## 💼 Use Cases


---

## 💻 Contributing

Your contributions are welcome! 
Have a look at the dedicated [contribution](DEVELOPMENT.md) section for further details.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
