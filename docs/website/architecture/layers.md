# 🏛️ System Architecture Layers

The tool is built strictly on **Clean Architecture Principles** (Onion Architecture). This guarantees structural
separation, high automated testability, and decoupling from external CLI frameworks or operating systems.

### Strict Dependency Direction

In keeping with the Clean Architecture Dependency Rule, all software source dependencies points **inward**. Inner layers
remain agnostic of outer layer data-structures or driver choices.

| Package Layer | Architecture Layer      | Core Strategic Domain Responsibility                                                                                              |
|:--------------|:------------------------|:----------------------------------------------------------------------------------------------------------------------------------|
| **`core`**    | Domain / Use Cases      | Captures pure business concepts (JDK discovery patterns, lifecycle definitions). Independent of running OS or runtime UI details. |
| **`cli`**     | Interface Adapters      | Low-profile translation context parsing arguments into explicit invocations of Core entities.                                     |
| **`infra`**   | Drivers / OS Components | Low-level drivers interacting directly with the OS environment (such as running processes or calling `keytool` binaries).         |

