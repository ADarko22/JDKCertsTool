# System Architecture

This project is built using Clean Architecture principles to guarantee high testability, maintainability, and complete
independence from external frameworks, CLI frameworks, and operating system details.

The architecture strictly adheres to the **Dependency Rule**: source code dependencies flow exclusively inward toward
higher-level policies.

1. **[core](https://www.google.com/search?q=src/main/kotlin/edu/adarko22/jdkcerts/core)** *(Domain / Use Cases)*:
   Contains core business rules (e.g., JDK discovery logic, truststore and certificate lifecycle management). It
   operates without dependencies on CLI parsing tools or OS execution primitives.
2. **[cli](https://www.google.com/search?q=src/main/kotlin/edu/adarko22/jdkcerts/cli)** *(Interface Adapters)*: Parses
   command-line inputs (Clikt commands), routes them to Core Use Cases, and formats domain outcomes for console display.
3. **[infra](https://www.google.com/search?q=src/main/kotlin/edu/adarko22/jdkcerts/infra)** *(Frameworks / Drivers)*:
   Handles low-level infrastructure operations, including filesystem scanning, OS environment inspection, and `keytool`
   process execution.

---

## High-Level Package Structure

| Package | Clean Architecture Layer | Responsibility                                                                                     |
|---------|--------------------------|----------------------------------------------------------------------------------------------------|
| `core`  | Domain / Use Cases       | Defines high-level policies, business entities, use cases, and outbound interfaces (Ports).        |
| `cli`   | Interface Adapters       | Handles user input parsing, presentation logic, and console formatting (Clikt integration).        |
| `infra` | Frameworks & Drivers     | Executes process calls (`keytool`), inspects system environment, and queries the local filesystem. |

---

## Key Design Patterns & Practices

### Command / Query Responsibility Segregation (CQRS)

Operations targeting `keytool` strictly separate state-mutating actions from read-only inspections:

* **Commands**: State-changing operations (e.g., `install`, `remove`).
* **Queries**: Read-only operations (e.g., `find`, `list`).

Each flow outputs a dedicated, strongly-typed domain model:

* `KeytoolCommandResult` (`Success`, `DryRun`, or typed `Failure`)
* `KeytoolQueryResult` (`Found`, `NotFound`, `DryRun`, or typed `Failure`)

Both domain outputs originate from a neutral `KeytoolProcessResult` emitted by the `infra` process runner. A centralized
`KeytoolErrorClassifier` parses raw process exit codes and stderr streams into domain-level failures. This isolates
domain results from process-level details while keeping error classification centralized.

---

## Architectural Verification

To prevent boundary leakage and enforce dependency rules automatically, architectural constraints are verified at build
time via JUnit tests located in `src/test/kotlin/edu/adarko22/jdkcerts/architecture`.

The project utilizes ArchUnit's **Freezing ArchRule** feature configured in `src/test/resources/archunit.properties`:

* The rule store snapshot is maintained under `src/test/resources/archunit_store`.
* Build pipelines automatically fail if any new architectural violations are introduced.

---

## Extensibility

The decoupled design allows for straightforward expansion across several vectors:

* **New Certificate Workflows**: Implement new Use Cases in `core` (e.g., truststore integrity validation, certificate
  export, cross-JDK synchronization) without altering CLI or infrastructure code.
* **Platform & Tool Adaptability**: Add alternative discovery mechanisms or OS platform integration (e.g., Windows
  Certificate Store adapters) in `infra` while leaving domain logic untouched.
* **Flexible Interfaces**: Swap or supplement the `cli` module with alternative presentation layers (e.g., a REST API,
  Web UI, or Daemon process) targeting the same `core` Use Cases.
* **Plug-and-Play Infrastructure**: Swap port implementations via dependency injection (e.g., replacing local process
  runners with remote execution agents or cloud listers).

---

## Evolution Toward Multi-Module

Currently, architectural boundaries are enforced within a single module via package structure and ArchUnit rules. Should
project complexity require it, transitioning this structure into dedicated Gradle subprojects (`:core`, `:cli`,
`:infra`) can be achieved without refactoring core business logic.