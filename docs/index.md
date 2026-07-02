# 🛠️ JDK Certs Tool

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A command-line utility to effortlessly manage certificates across all discovered JDK installations on your machine.

---

## Why JDKCertsTool?

Modern JDKs rely on a **trusted certificate store** to securely connect to HTTPS services. Managing these
certificates—especially across multiple isolated Java distributions—manually via `keytool` can be tedious and
error-prone.

If you’ve ever hit SSL or trust errors (`PKIX path building failed`) when using Java with internal enterprise services,
local proxies, or corporate web gateways, **this tool helps you fix that in seconds.**

### Key Capabilities

- ✅ **Global Scope:** Add or remove custom certificates across all installed JDKs in a single action.
- 🔍 **Smart Search:** Find certificates by exact-match, regex pattern, or fuzzy matching.
- ⚙️ **Parallel Processing:** Executes operations concurrently across JDKs for lightning-fast throughput.
- 🛡️ **Safe Previews:** Includes `--dry-run` safety mechanisms across all mutating operations.

!!! note "Environment Assumption"
    This is a developer-focused tool optimized for development environments. It assumes a uniform keystore configuration
    across discovered paths, using standard keystore passwords (defaulting to `changeit`) and targeting the default *
    *cacerts** keystore.