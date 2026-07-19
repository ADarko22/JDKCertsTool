# 🏛️ System Architecture Layers

The tool is built strictly on **Clean Architecture Principles** (Onion Architecture). This guarantees structural
separation, high automated testability, and decoupling from external CLI frameworks or operating systems.

## Strict Dependency Direction

In keeping with the Clean Architecture Dependency Rule, all software source dependencies points **inward**. Inner layers
remain agnostic of outer layer data-structures or driver choices.

| Package Layer | Architecture Layer      | Core Strategic Domain Responsibility                                                                                              |
|:--------------|:------------------------|:----------------------------------------------------------------------------------------------------------------------------------|
| **`core`**    | Domain / Use Cases      | Captures pure business concepts (JDK discovery patterns, lifecycle definitions). Independent of running OS or runtime UI details. |
| **`cli`**     | Interface Adapters      | Low-profile translation context parsing arguments into explicit invocations of Core entities.                                     |
| **`infra`**   | Drivers / OS Components | Low-level drivers interacting directly with the OS environment (such as running processes or calling `keytool` binaries).         |

## Command Query Responsibility Segregation (CQRS)

To prevent business logic leaks and eliminate unnecessary boilerplates, the core layer follows the CQRS principle for
modeling the keytool operations performed by the tool.

The base `KeytoolOperation` abstraction is specialized into two sealed interfaces that separate mutations from reads.

```
                  ┌─── KeytoolOperation ───┐
                  │                        │
         [Mutations]                      [Reads]
     KeytoolCommand                   KeytoolQuery
          │                                │
  ┌───────┴───────┐                        │
Install         Remove                    Find

```

1. **KeytoolCommand (Mutations)**
   Represents an intent to change the state of a keystore (e.g., `InstallCertKeytoolCommand`,
   `RemoveCertKeytoolCommand`).

2. **KeytoolQuery (Reads)**
   Represents an intent to inspect a keystore without mutating state (e.g., `FindCertKeytoolQuery`).

At the infrastructure layer (`infra`), both paths converge onto the concurrent `KeytoolProcessRunner`. It executes each
operation and returns a **verdict-free** `KeytoolProcessResult` — either `Executed` (raw exit code, stdout, stderr) or
`DryRun` (the previewed command). This boundary type carries no success/failure judgement and no OS/process types, so
the domain stays agnostic of infrastructure details.

Interpretation happens back in the `core` layer:

- A single `KeytoolErrorClassifier` translates a failed execution's raw output into a neutral `KeytoolFailure`
  (e.g. `WrongPassword`, `AliasNotFound`, `AliasAlreadyExists`, `CertificateAlreadyExists`, `Unknown`). This is the one
  place keytool's output strings are matched.
- Each CQRS use case then maps the outcome into its own domain result:
    - the **command** path (`ExecuteKeytoolCommandUseCase`) produces a `KeytoolCommandResult`
      (`Success` / `DryRun` / typed `Failure`);
    - the **query** path (`FindKeytoolCertificateUseCase`) produces a `KeytoolQueryResult`
      (`Found` / `NotFound` / `DryRun` / typed `Failure`), and additionally owns the heavy text work — regex filtering
      and fuzzy-match scoring on certificate aliases.

CQRS isolates that heavy query-side parsing math inside the read path, while dry-run is modelled as a first-class result
on both sides, leaving the mutation engine lightweight and maintainable.

