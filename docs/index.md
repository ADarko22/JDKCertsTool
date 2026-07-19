# 🛠️ JDK Certs Tool

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A command-line utility to effortlessly manage certificates across all discovered JDK installations on your machine.

---

## Why JDKCertsTool?

Modern JDKs rely on a **trusted certificate store** to securely connect to Secured Services. Managing manually these
certificates, across multiple isolated Java distributions, via `keytool` can be tedious and error-prone.

If you’ve ever hit SSL or trust errors (`PKIX path building failed`) when using Java with internal enterprise services 
or corporate firewalls, **this tool helps you fix that in seconds.**

### Key Capabilities

- ✅ **Global Scope:** Add or remove custom certificates across all installed JDKs in a single action.
- 🔍 **Smart Search:** Find certificates by exact-match, regex pattern, or fuzzy matching.
- ⚙️ **Parallel Processing:** Executes operations concurrently across JDKs for lightning-fast throughput.
- 🛡️ **Safe Previews:** Includes `--dry-run` safety mechanisms across all mutating operations.

!!! info "Scope: the JDK truststore, not the application keystore"
    JDKCertsTool manages the JDK **truststore** only — the `TrustedCertEntry` entries that hold the **public
    certificates of external services** (i.e. HTTP clients, secure DB connections, corporate proxy/firewall CAs, etc.). 
    It works on the default **`cacerts`** truststore (JDKs > 8) or resolves the default truststore path on older JDKs.

    It is **not** concerned with the application **keystore** — `PrivateKeyEntry` entries (i.e. TLS/HTTPS private keys, 
    JWT signing keys, etc.). Those hold private material and **must be managed securely by the application itself**; 
    they are explicitly out of scope.

!!! note "Environment Assumption"
    This is a developer-focused tool optimized for development environments. It assumes a uniform truststore
    configuration across discovered paths, using standard passwords (defaulting to `changeit`) and targeting the default
    **cacerts** truststore.