# 📋 list-jdks

Discovers and prints a tabular inventory of all Java Development Kit (JDK) installations detected on the system.

### Usage
```bash
jdkcerts list-jdks [--custom-jdk-paths <VALUE>]
```

### Options

| Option | Description |
| --- | --- |
| `--custom-jdk-paths <VALUE>` | Comma-separated absolute JDK **home paths** to target explicitly (bypasses system scanning). |
| `-h, --help` | Show help context and exit. |

### Example

```bash
jdkcerts list-jdks --custom-jdk-paths "/Users/you/.sdkman/candidates/java/11.0.28-sem, /Users/you/.sdkman/candidates/java/8.0.472-zulu"
```

Provide a quoted, comma-separated list of absolute JDK home paths (`~` is expanded, whitespace trimmed).
