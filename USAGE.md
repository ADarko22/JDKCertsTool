# Commands Overview

> ⚠️ **Default Scanning vs Custom Override Mode:**
>
> Using the `--custom-jdk-paths` option disables automatic system scanning.
> The tool then runs strictly on the JDK home paths you provide, keeping your execution isolated and safe.
>
> Provide the value as a **quoted, comma-separated list of absolute JDK home paths**:
>
> ```bash
> jdkcerts list-jdks --custom-jdk-paths "/Users/you/.sdkman/candidates/java/11.0.28-sem, /Users/you/.sdkman/candidates/java/8.0.472-zulu"
> ```
>
> (`~` is expanded automatically and surrounding whitespace is trimmed.)

## ℹ️ info

Displays basic project and environment info.

```bash
jdkcerts info
```

**Options:**

| Option     | Description                |
|------------|----------------------------|
| -h, --help | Show this message and exit |

---

## 📋 list-jdks

Lists all discovered JDK installations.

```bash
jdkcerts list-jdks [--custom-jdk-paths <VALUE>]
```

**Options:**

| Option                     | Description                                                                  |
|----------------------------|------------------------------------------------------------------------------|
| --custom-jdk-paths <VALUE> | Comma-separated absolute JDK home paths (optional). Bypasses default scanning. |
| -h, --help                 | Show this message and exit                                                    |

---

## 📥 install-cert

Installs a certificate across all discovered JDK keystores.

```bash
jdkcerts install-cert --cert <PATH> --alias <ALIAS> [--keystore-password <PASSWORD>] [--dry-run] [--custom-jdk-paths <VALUE>]
```

**Options:**

| Option                     | Description                                                                    | Default    |
|----------------------------|--------------------------------------------------------------------------------|------------|
| --cert <VALUE>             | Path to the certificate file (**required**)                                    |            |
| --alias <TEXT>             | Certificate alias (**required**)                                               |            |
| --keystore-password <TEXT> | Keystore password                                                              | `changeit` |
| --custom-jdk-paths <VALUE> | Comma-separated absolute JDK home paths (optional). Bypasses default scanning. |            |
| --dry-run                  | Preview changes without modifying anything                                     |            |
| -h, --help                 | Show this message and exit                                                     |            |

**Example:**

```bash
# Preview installation
jdkcerts install-cert --cert /path/to/cert.pem --alias my-cert --dry-run

# Actually install with default password
jdkcerts install-cert --cert /path/to/cert.pem --alias my-cert

# Restrict to specific JDK installations
jdkcerts install-cert --cert /path/to/cert.pem --alias my-cert \
  --custom-jdk-paths "/Users/you/.sdkman/candidates/java/11.0.28-sem, /Users/you/.sdkman/candidates/java/8.0.472-zulu"
```

---

## 🗑️ remove-cert

Removes a certificate by alias from all discovered JDK keystores.

```bash
jdkcerts remove-cert --alias <ALIAS> [--keystore-password <PASSWORD>] [--dry-run] [--custom-jdk-paths <VALUE>]
```

**Options:**

| Option                     | Description                                                                    | Default    |
|----------------------------|--------------------------------------------------------------------------------|------------|
| --alias <TEXT>             | Certificate alias (**required**)                                               |            |
| --keystore-password <TEXT> | Keystore password                                                              | `changeit` |
| --custom-jdk-paths <VALUE> | Comma-separated absolute JDK home paths (optional). Bypasses default scanning. |            |
| --dry-run                  | Preview changes without modifying anything                                     |            |
| -h, --help                 | Show this message and exit                                                     |            |

**Example:**

```bash
# Preview removal
jdkcerts remove-cert --alias my-cert --dry-run

# Actually remove
jdkcerts remove-cert --alias my-cert
```

---

## 🔍 find-cert

Finds and displays certificate details by alias across all JDK keystores.

Supports three search strategies for maximum flexibility:

- **EXACT_MATCH** (default): Fast direct alias lookup using keytool's `-alias` option
- **REGEX**: Pattern-based matching across all keystore entries (use `--regex` flag)
- **CLOSEST_MATCH**: Fuzzy matching for approximate alias names (use `--closest-match` flag)

```bash
jdkcerts find-cert --alias <ALIAS> [--keystore-password <PASSWORD>] [--verbose] [--regex|--closest-match] [--custom-jdk-paths <VALUE>]
```

**Options:**

| Option                     | Description                                                                    | Default    |
|----------------------------|--------------------------------------------------------------------------------|------------|
| --alias <TEXT>             | Certificate alias or search pattern (**required**)                             |            |
| --keystore-password <TEXT> | Keystore password                                                              | `changeit` |
| --verbose                  | Display all certificate details (SHA1, SHA256, Serial, etc.)                   | `false`    |
| --regex                    | Search by regex pattern instead of exact match                                 |            |
| --closest-match            | Search by closest match (fuzzy matching for typos)                             |            |
| --custom-jdk-paths <VALUE> | Comma-separated absolute JDK home paths (optional). Bypasses default scanning. |            |
| -h, --help                 | Show this message and exit                                                     |            |

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

**Notes:**

- The `--regex` and `--closest-match` flags cannot be used together.
- The`closest-match` strategy is case-insensitive and uses a default similarity threshold of `0.3`.
