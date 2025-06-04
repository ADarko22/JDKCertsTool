# 🛠️ JDK Certs Tool

A command-line tool to manage JDK certificates. It allows you to **add**, **remove**, or **list** certificates in the JDK's keystore.

The tool internally uses the JDK's built-in `keytool` command to manage certificates.

---

## 🛠️ Installation

### Via Homebrew (Mac/Linux)

```bash
brew tap ADarko22/JDKCertsTool https://github.com/ADarko22/JDKCertsTool
brew install jdkcerts
````

### 🚀 Usage

Use the `jdkcerts` command to run the tool.

```bash
jdkcerts --help
```

---

### Cloning the repository

```bash
git clone https://github.com/ADarko22/JDKCertsTool.git
```

#### 🚀 Usage

From the cloned repository, you can run the tool using Gradle:

```bash
./gradlew run --args="--help"
````

---

## ❓ Usage Commands

### 📋 List JDKs

**Command:** `list-jdk`

**Options:**

| Option                      | Description                                             |
|-----------------------------|---------------------------------------------------------|
| `-h`, `--help`              | ❓ Show this message and exit                            |
| `--custom-jdk-dirs <VALUE>` | 🗂️ Comma-separated paths to JDK directories (optional) |

---

### 📥 Install a Certificate

**Command:** `install-cert`

**Options:**

| Option                       | Description                                             | Default       |
|------------------------------|---------------------------------------------------------|---------------|
| `-h`, `--help`               | ❓ Show this message and exit                            |               |
| `--custom-jdk-dirs <VALUE>`  | 🗂️ Comma-separated paths to JDK directories (optional) |               |
| `--cert <VALUE>`             | 📄 Path to the certificate file (required)              |               |
| `--keystore-password <TEXT>` | 🔐 Keystore password                                    | `changeit`    |
| `--alias <TEXT>`             | 🏷️ Certificate alias                                   | `custom-cert` |
| `--dry-run`                  | 🛑 Preview changes without modifying anything           |               |

---

### 🗑️ Remove a Certificate

**Command:** `remove-cert`

**Options:**

| Option                       | Description                                             | Default       |
|------------------------------|---------------------------------------------------------|---------------|
| `-h`, `--help`               | ❓ Show this message and exit                            |               |
| `--custom-jdk-dirs <VALUE>`  | 🗂️ Comma-separated paths to JDK directories (optional) |               |
| `--keystore-password <TEXT>` | 🔐 Keystore password                                    | `changeit`    |
| `--alias <TEXT>`             | 🏷️ Certificate alias                                   | `custom-cert` |
| `--dry-run`                  | 🛑 Preview changes without modifying anything           |               |

---

