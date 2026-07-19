# 💡 Commands Overview

The `jdkcerts` CLI offers a specialized suite of commands targeting the entire certificate lifecycle.

### Global Execution Safeties

!!! warning "Default Scanning vs Custom Override Mode"
    By default, the tool scans common system paths to auto-discover installations. 
    Passing the `--custom-jdk-paths` option **completely disables** automatic scanning. 
    The tool will strictly isolate its scope to the exact JDK home paths you supply, as a quoted,
    comma-separated list — e.g. `--custom-jdk-paths "/path/to/jdk-11, /path/to/jdk-8"`.
