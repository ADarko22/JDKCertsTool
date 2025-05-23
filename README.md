# 🛠️ JDK Certs Tool

A command-line tool to manage JDK certificates. It allows you to **add**, **remove**, or **list** certificates in the JDK's keystore.

The tool internally uses the JDK's built-in `keytool` command to manage certificates.

---

## 🚀 Usage

### ❓ Show Help

```bash
./gradlew run --args="--help"
````

---

### 📋 List JDKs

```bash
./gradlew run --args="list-jdk --custom-jdk-dirs <VALUE>"
```

**Options:**

| Option                      | Description                                             |
| --------------------------- | ------------------------------------------------------- |
| `-h`, `--help`              | ❓ Show this message and exit                            |
| `--custom-jdk-dirs <VALUE>` | 🗂️ Comma-separated paths to JDK directories (optional) |

---

### 📥 Install a Certificate

```bash
./gradlew run --args="install-cert --cert <CERT_PATH> --alias <ALIAS> --custom-jdk-dirs <VALUE> --dry-run"
```

**Options:**

| Option                       | Description                                             | Default       |
| ---------------------------- | ------------------------------------------------------- | ------------- |
| `-h`, `--help`               | ❓ Show this message and exit                            |               |
| `--custom-jdk-dirs <VALUE>`  | 🗂️ Comma-separated paths to JDK directories (optional) |               |
| `--cert <VALUE>`             | 📄 Path to the certificate file (required)              |               |
| `--keystore-password <TEXT>` | 🔐 Keystore password                                    | `changeit`    |
| `--alias <TEXT>`             | 🏷️ Certificate alias                                   | `custom-cert` |
| `--dry-run`                  | 🛑 Preview changes without modifying anything           |               |

---

### 🗑️ Remove a Certificate

```bash
./gradlew run --args="remove-cert --alias <ALIAS> --custom-jdk-dirs <VALUE> --dry-run"
```

**Options:**

| Option                       | Description                                             | Default       |
| ---------------------------- | ------------------------------------------------------- | ------------- |
| `-h`, `--help`               | ❓ Show this message and exit                            |               |
| `--custom-jdk-dirs <VALUE>`  | 🗂️ Comma-separated paths to JDK directories (optional) |               |
| `--keystore-password <TEXT>` | 🔐 Keystore password                                    | `changeit`    |
| `--alias <TEXT>`             | 🏷️ Certificate alias                                   | `custom-cert` |
| `--dry-run`                  | 🛑 Preview changes without modifying anything           |               |

---

