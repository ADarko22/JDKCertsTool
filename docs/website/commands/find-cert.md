# 🔍 find-cert

Queries keystores using one of three pluggable matching engines to trace and debug profile layouts.

### Lookup Strategies

1. **`EXACT_MATCH` (Default):** Ultra-fast, direct lookup using native `keytool` pipeline mappings.
2. **`REGEX`:** Evaluates an expressions mask against all aliases (triggered via `--regex`).
3. **`CLOSEST_MATCH`:** Implements case-insensitive Levenshtein/fuzzy distances to gracefully resolve user typos (triggered via `--closest-match`).

### Usage
```bash
jdkcerts find-cert --alias <ALIAS> [options]
```

### Options

| Option | Description | Default |
| --- | --- | --- |
| `--alias <TEXT>` | **Required** Lookup string token or regex matching sequence. |  |
| `--verbose` | Emits complete certificate structures (SHA-1/256 signatures, Serial IDs). | `false` |
| `--regex` | Evaluates lookup input as a regular expression pattern mask. |  |
| `--closest-match` | Switches lookup to fuzzy evaluation logic (similarity threshold `0.3`). |  |

!!! error "Exclusivity Constraint"
    The `--regex` and `--closest-match` modifiers are mutually exclusive. Triggering both simultaneously will yield an options parsing error.

### Examples

```bash
# Detailed verbose investigation
jdkcerts find-cert --alias production-api-gateway --verbose

# Regular expression lookup across stores
jdkcerts find-cert --alias ".*internal.*" --regex

# Typos mitigation
jdkcerts find-cert --alias "stg-cert" --closest-match
```