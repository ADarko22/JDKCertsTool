# 🗑️ remove-cert

Purges a specific certificate entry out of target JDK truststores matching a specified string alias.

### Usage
```bash
jdkcerts remove-cert --alias <ALIAS> [options]
```

### Options

| Option | Type | Description | Default |
| --- | --- | --- | --- |
| `--alias <TEXT>` | **Required** | The exact alphanumeric alias identifying the certificate to drop. |  |
| `--keystore-password <TEXT>` | Optional | Password protecting the target truststores. | `changeit` |
| `--custom-jdk-paths <VALUE>` | Optional | Scopes target operations exclusively to the provided absolute JDK home paths. |  |
| `--dry-run` | Flag | Logs matching targets and exits without mutating truststores on disk. |  |

### Examples

```bash
# Perform a safety audit trace
jdkcerts remove-cert --alias legacy-dev-root --dry-run

# Delete target alias everywhere
jdkcerts remove-cert --alias legacy-dev-root
```