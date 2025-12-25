# System Architecture

This project is built using the Clean Architecture principles (also known as Onion Architecture), ensuring high
testability, maintainability, and independence from external frameworks (like the CLI or specific operating systems).

The architecture is strictly separated into three layers, adhering to the Dependency Rule (dependencies only flow
inward):

1. [core](src/main/kotlin/edu/adarko22/jdkcerts/core) (Domain/Use Cases): Contains the business logic (e.g., the
   rules for discovering a JDK, for managing a certificate). It is entirely decoupled from the OS and CLI.

2. [cli](src/main/kotlin/edu/adarko22/jdkcerts/cli) (Interface Adapters): Translates user input (CLI arguments) into
   calls to the Core Use Cases, and formats the Core's output for display.

3. [infra](src/main/kotlin/edu/adarko22/jdkcerts/infra) (Drivers/OS): Contains the concrete, low-level implementations
   for interacting with the operating system (e.g., executing the keytool binary, scanning the filesystem).

For a detailed breakdown of the layers and components, please refer to the dedicated documentation: Project Architecture
Contextualization.

## High-Level Package Structure

| Package | Clean Architecture Layer | Responsibility                                                                                   |
|---------|--------------------------|--------------------------------------------------------------------------------------------------|
| core    | Domain / Use Cases       | Defines the "What" (Entities, Use Cases, Ports/Interfaces).                                      |
| cli     | Interface Adapters       | Defines the "How" for the Command Line Interface (Clikt commands, presenters).                   |
| infra   | Frameworks / Drivers     | Defines the "How" for the Operating System (e.g., UNIXSystemInfoProvider, DefaultProcessRunner). |

## Extensibility

The Clean Architecture principles used in this tool make sure that new features, platforms, and behaviors can be added
without modifying existing core logic. Each layer has a single responsibility and depends only on the layers inward.

Here are some ways the architecture supports extensibility:

- **New Certificate Workflows**

  Add high-level operations—like exporting certificates, validating keystore integrity, or syncing certs between JDKs—by
  simply adding new Use Cases in the `core` layer.

- **Platform & Tool Adaptability**

  Support different environments (like Windows Certificate Stores) or alternative discovery methods by implementing new
  adapters in the `infra` layer without changing a single line of business logic.

- **Flexible Interfaces**

  Because the `cli` layer is a thin wrapper, you can easily add new commands or even replace the CLI entirely with a
  REST
  API or Web UI that interacts with the same `core` logic.

- **Plug-and-Play Implementations**

  Swap low-level behaviors by implementing existing interfaces (Ports). For example, you could replace the local
  filesystem scanner with a cloud-based JDK locator or a remote execution agent.

This architecture ensures that JDKCertsTool remains adaptable, maintainable, and open to future expansion,
whether that means new platforms, new certificate workflows, or deeper integrations in enterprise environments.

### Evolution Toward Multi-Module

Currently, this architecture is enforced through a package structure within a single root module. As the project grows,
transitioning to a multi-module setup (e.g., Gradle/Maven subprojects) would be straightforward.
