# 📥 install-cert

Concurrently imports a specific certificate asset into all discovered or designated JDK target keystores.

### Usage
```bash
jdkcerts install-cert --cert <PATH> --alias <ALIAS> [options]
```

### Options

| Option | Type | Description | Default |
| --- | --- | --- | --- |
| `--cert <PATH>` | **Required** | Local filesystem path to the target certificate (`.pem`, `.crt`, etc.) |  |
| `--alias <TEXT>` | **Required** | The target lookup entry alias to assign inside the keystore. |  |
| `--keystore-password <TEXT>` | Optional | The protection password guarding the targeted cacerts file. | `changeit` |
| `--custom-jdk-dirs <VALUE>` | Optional | Limits execution to specific, comma-separated JDK directory paths. |  |
| `--dry-run` | Flag | Simulates processing and previews updates without applying disk changes. |  |

### Examples

```bash
# Preview modifications safely
jdkcerts install-cert --cert /path/to/corp-ca.pem --alias corporate-root --dry-run

# Commit global installation
jdkcerts install-cert --cert /path/to/corp-ca.pem --alias corporate-root
```