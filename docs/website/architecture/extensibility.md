# Extensibility & Future Evolution

### Modular Capabilities

- **New Workflows:** Introducing features like certificate synchronization or exports involves writing standalone `core`
  use cases without touching external parsing or driver logic.
- **Alternative Drivers:** Porting drivers to handle alternate architectures (e.g., Windows Certificate Stores) is
  achievable by injecting an alternate implementation profile inside the `infra` layer.
- **Alternative Interfaces:** The thin design of the `cli` wrapper enables swapping or supplementing the interface
  engine with a local REST API daemon or a GUI frontend down the line.

### Evolution Toward Multi-Module

Currently, structural boundaries are maintained through clean packaging structures within a single root folder. As code
complexity grows, these layers can be split directly into isolated Gradle/Maven submodules.
