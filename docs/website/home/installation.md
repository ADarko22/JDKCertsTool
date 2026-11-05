# 📦 Installation

`jdkcerts` ships as a
self-contained [GraalVM native image](https://www.graalvm.org/latest/reference-manual/native-image/) —
**no JDK or JRE is required to run it**.

## Platform Support

| OS      | Architecture          | Notes                                                                 |
|---------|-----------------------|--------------------------------------------------------------------------|
| macOS   | Apple Silicon (arm64) |                                                                            |
| macOS   | Intel (x86_64)        |                                                                            |
| Linux   | x86_64                | Built on Ubuntu 22.04 (glibc 2.35) — needs a distro with glibc 2.35+ |
| Windows | —                     | Not currently supported                                              |

Linux arm64 isn't published yet — not worth the added build/matrix complexity for a low-adoption tool right now.

## Via Homebrew (macOS/Linux)

```bash
brew tap ADarko22/tap
brew install jdkcerts
```

## Manual / Direct Download

If you're not using Homebrew, download the archive matching your OS/architecture from the
[latest GitHub release](https://github.com/ADarko22/JDKCertsTool/releases/latest):

```bash
# Example for Apple Silicon macOS — substitute darwin-amd64 or linux-amd64 as needed
curl -LO https://github.com/ADarko22/JDKCertsTool/releases/latest/download/JDKCertsTool-<version>-darwin-arm64.tar.gz
tar -xzf JDKCertsTool-<version>-darwin-arm64.tar.gz
chmod +x jdkcerts
sudo mv jdkcerts /usr/local/bin/
```

!!! note "macOS Gatekeeper"
    The binary isn't code-signed. If macOS blocks it with "cannot verify developer" on first run, clear the
    quarantine attribute: `xattr -d com.apple.quarantine /path/to/jdkcerts`. This is generally not needed when
    installing via `brew install`.

## Alternative: Fat JAR (if you already have Java 25+)

Every release also publishes a self-contained `JDKCertsTool-<version>.jar` as a smaller alternative to the native binary 
— **only worth it if you already have a Java 25 or newer runtime installed**, since it needs one to run. 

Make sure you invoke `java -jar` with a JDK **25+** specifically:

```bash
curl -LO https://github.com/ADarko22/JDKCertsTool/releases/latest/download/JDKCertsTool-<version>.jar

# Point explicitly at a Java 25+ runtime, e.g. one managed by SDKMAN!
~/.sdkman/candidates/java/25.0.4-amzn/bin/java -jar JDKCertsTool-<version>.jar --help
```

Running it with an older JDK fails fast with `UnsupportedClassVersionError`.

### Quick Verification

Verify your installation by printing the global help menu:

```bash
jdkcerts --help
```
