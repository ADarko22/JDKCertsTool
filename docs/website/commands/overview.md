# 💡 Commands Overview

The `jdkcerts` CLI offers a specialized suite of commands targeting the entire certificate lifecycle.

### Global Execution Safeties

!!! warning "Default Scanning vs Custom Override Mode"
    By default, the tool scans common system paths to auto-discover installations. 
    Passing the `--custom-jdk-dirs` option **completely disables** automatic scanning. 
    The tool will strictly isolate its scope to the exact directory paths you supply.

### Available commands
- [info](info.md)
- [list-jdks](list-jdks.md)
- [find-cert](find-cert.md)
- [install-cert](install-cert.md)
- [remove-cert](remove-cert.md)