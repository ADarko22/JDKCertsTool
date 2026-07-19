# 💼 Use Cases

JDKCertsTool addresses common real-world scenarios where managing JDK truststores becomes a bottleneck. Rather than
manually fighting with `keytool` for every Java version on your machine, this tool manages the entire lifecycle of your
certificates globally.

All scenarios below concern the JDK **truststore** — the `TrustedCertEntry` public certificates of external services
(databases, proxies, etc.). Application **private keys** (`PrivateKeyEntry`) are out of scope and must be managed by the
application itself.

## Enterprise Setup & Provisioning

**Problem:** Corporate environments frequently use SSL inspection via internal Certificate Authorities (CAs), or
developers rely on self-signed certificates for local staging servers. If your JDKs don't trust these certificates,
IDEs, build tools (Maven/Gradle), and local applications will throw SSL connection errors.
Modern developers also commonly have multiple JDK distributions installed simultaneously, making certificate management
tedious and error-prone.

**Solution:** Use `list-jdks` to verify your environment footprint, then inject a required corporate or local
certificate into all discovered JDKs in a single operation.

```bash
jdkcerts install-cert --cert /path/to/corp-ca.pem --alias corp-ca --dry-run
```

## Troubleshooting Trust Errors

**Problem:** The dreaded `PKIX path building failed` error appears, but you're not sure whether:

- The required certificate is missing
- The certificate has expired
- The certificate was installed under an unexpected alias

**Solution:** Use flexible search strategies to locate certificates even when you only remember part of the alias or
made a typo during installation.

```bash
jdkcerts find-cert --alias "dev-srvr" --closest-match --verbose
```

## Auditing & Security Hygiene

**Problem:** Over time, developer workstations and CI/CD runners accumulate obsolete, unused, or expired certificates.
Retaining compromised or expired root CAs increases security risk and can lead to unexpected runtime failures.

**Solution:** Perform periodic audits using regex-based searches to identify related certificate groups, then remove
obsolete entries from every JDK on the system with a single command.

```bash
jdkcerts find-cert --alias ".*legacy.*" --regex
jdkcerts remove-cert --alias legacy-dev-2023
```
