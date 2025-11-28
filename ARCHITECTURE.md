# System Architecture

This project is built using the Clean Architecture principles (also known as Onion Architecture), ensuring high
testability, maintainability, and independence from external frameworks (like the CLI or specific operating systems).

The architecture is strictly separated into three layers, adhering to the Dependency Rule (dependencies only flow
inward):

1. [core](src/main/kotlin/edu/adarko22/jdkcerts/core) (Domain/Use Cases): Contains the business logic (e.g., the
   rules for discovering a JDK, for a managing a certificate). It is entirely decoupled from the OS and CLI.

2. [cli](src/main/kotlin/edu/adarko22/jdkcerts/cli) (Interface Adapters): Translates user input (CLI arguments) into
   calls to the Core Use Cases, and formats the Core's output for display.

3. [infra](src/main/kotlin/edu/adarko22/jdkcerts/infra) (Drivers/OS): Contains the concrete, low-level implementations
   for interacting with the operating system (e.g., executing the keytool binary, scanning the filesystem).

For a detailed breakdown of the layers and components, please refer to the dedicated documentation: Project Architecture
Contextualization.

## High-Level Package Structure

| Package        | Clean Architecture Layer | Responsibility                                                                                   |
|----------------|--------------------------|--------------------------------------------------------------------------------------------------|
| core           | Domain / Use Cases       | Defines the "What" (Entities, Use Cases, Ports/Interfaces).                                      |
| cli            | Interface Adapters       | Defines the "How" for the Command Line Interface (Clikt commands, presenters).                   |
| infrastructure | Frameworks / Drivers     | Defines the "How" for the Operating System (e.g., UNIXSystemInfoProvider, DefaultProcessRunner). |

## Extensibility

The Clean Architecture principles used in this tool make sure that new features, platforms, and behaviors can be added
without modifying existing core logic. Each layer has a single responsibility and depends only on the layers inward.

Here are some ways the architecture supports extensibility:

- **Add new certificate operations easily**

  New use cases—such as exporting certificates, validating cacerts keystores, or synchronizing certs across machines—can
  be added in the core layer without touching CLI or OS-specific code.

- **Plug in new operating-system or tool integrations**

  Need to support a different keytool invocation strategy, Windows certificate stores, or alternate file-system
  scanning?
  Implement new adapters in the infra layer without changing business rules.

- **Extend the CLI safely**

  New commands or flags can be introduced in the cli layer while reusing the same core interfaces and use cases.
  The CLI remains thin, predictable, and easy to evolve.

- **Swap implementations without breaking the tool**

  Core use cases define the required ports. As long as a new adapter implements the same port, it can replace existing
  behavior (e.g., a new way to detect JDK installations).

This architecture ensures that JDKCertsTool remains adaptable, maintainable, and open to future expansion,
whether that means new platforms, new certificate workflows, or deeper integrations in enterprise environments.