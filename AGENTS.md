# judo-psm-generator-jaxrs — module agent doctrine

## Module purpose

`judo-psm-generator-jaxrs` is a **template set**, not a running service: it ships
Handlebars templates plus `@TemplateHelper` classes that the judo generator
engine loads to turn a JUDO PSM model into a compilable Jakarta RESTful Web
Services (JAX-RS) source tree. It owns no `main()` and no HTTP runtime — its
deliverable is the `psm-jaxrs.yaml` template descriptors bundled in each
sub-artifact, which the consuming generator plugin discovers, merges, and
executes against the model.

**Which PSM elements become endpoints.** The descriptors bind template files to
model queries through SpEL factory expressions, so the model drives the emitted
surface one-to-one:

- `#allExposedTransferObjectWithOperation(#model)` → one JAX-RS service class
  per exposed transfer object that carries operations. Each PSM operation on it
  becomes one resource method.
- `#allAccessPointActor(#model)` → one `Application` configuration class per
  access-point actor (the deployable REST application per actor).
- `#allTransferObject(#model)` → three DTO shapes per transfer object —
  response, request, create-request — because the wire contract differs per
  direction.
- `#allRange(#model)` → range-request DTOs; `#allEnumType(#model)` → Java enums;
  `#allQueryCustomizer(#model)` → query-customizer DTOs.
- `#model` (once per model) → the shared `Error` and `Detail` fault types, and
  the impl-layer `RestUtils`.

**What the generated API surface looks like.** A generated operation method is
annotated `@javax.ws.rs.GET` when the PSM operation is stateless with no input
parameter, otherwise `@POST` with `@Consumes("application/json")`; it
`@Produces` `application/json`, or `APPLICATION_OCTET_STREAM` for an export
operation. Its `@javax.ws.rs.Path` is derived from the PSM behaviour type by
`OperationHelper`, and it carries
`@hu.blackbelt.judo.dispatcher.api.JudoOperation("<asm fq name>")` so the
runtime dispatcher can route the call back to the model operation. Every method
returns `javax.ws.rs.core.Response` and accepts the JUDO protocol headers
`X-Judo-CountRecords`, `X-Judo-Mask`, `X-Judo-RequestParameters` — plus
`X-Judo-MarkSelectedRangeItems` for range operations and `X-Judo-SignedIdentifier`
for bound operations. Payloads cross the boundary as
`Map<String, Object>` / `List<Map<String, Object>>`; the generated DTO classes
exist to describe the schema (OpenAPI/Swagger annotations, optional) rather than
to be bound directly.

Generation is layered on purpose: the `api` layer emits the interface + DTOs,
`impl` emits the dispatcher-backed `*Impl` classes, and `osgi` emits `*Component`
wrappers that instantiate the impl, bind the `Dispatcher`, and register the API
interface as an OSGi service. A consumer can take `api` alone (for a client),
`api`+`impl` (for a plain container), or all three (for an OSGi runtime).

**Repository:** BlackBeltTechnology/judo-psm-generator-jaxrs ·
**License:** EPL-2.0 · **Java:** 21 · **Maven:** 3.9.4, OSGi bundle packaging.

## Reactor map

Root `pom.xml` is an aggregator declaring five `<modules>`; every one packages
as `bundle` (`maven-bundle-plugin`) and inherits `${revision}` CI-friendly
versioning from the root POM.

| Reactor module | What it contributes |
|---|---|
| `judo-psm-generator-jaxrs-common` | Fragments every other layer includes — chiefly the EPL-2.0 license header partial pulled in as `{{> fragment.header.hbs }}` at the top of each generated file. Its `psm-jaxrs.yaml` declares an empty `templates:` list on purpose: it emits nothing itself, it only supplies includes. |
| `judo-psm-generator-jaxrs-api` | The API layer, and the largest descriptor — ten template bindings covering the REST service interface, the response/request/create-request/range/query-customizer DTOs, enums, the per-actor `Application` config, and the model-wide `Error`/`Detail` types. Hosts eight `@TemplateHelper` classes (`AttributeHelper`, `JavaApiHelper`, `JavaNamespaceHelper`, `ModelHelper`, `ObjectTypeHelper`, `OperationHelper`, `RelationHelper`, `StoredVariableHelper`) that every layer's SpEL expressions resolve against. |
| `judo-psm-generator-jaxrs-impl` | The implementation layer — three bindings: `*Impl` service classes that delegate to the JUDO `Dispatcher`, the per-actor application-config implementation, and a single model-wide `RestUtils`. Helper: `JavaImplHelper`. |
| `judo-psm-generator-jaxrs-osgi` | The OSGi layer — two bindings emitting `@Component` wrappers that construct the impl class, bind the `Dispatcher`, and `registerService` the API interface, plus the per-actor OSGi application config. Helper: `JavaOsgiHelper`. |
| `judo-psm-generator-jaxrs-test` | Guards the DTO wire contract rather than the templates: Jackson `ObjectMapper` + `Jdk8Module` round-trip tests proving `Optional`-typed fields serialize as the generated code assumes. Carries the PSM fixture models under `src/test/resources/model`. |

A `judo-psm-generator-jaxrs-guice` directory may exist on disk with only a
`target/` tree — it is stale build output from a removed reactor module, is not
listed in `<modules>`, and must not be treated as live source.

## Build commands

```bash
mvn clean install                    # Full build with tests
mvn clean test                       # Run tests only
mvn test -Dtest=SerializationTest    # Run a single test
mvn clean install -Pmodules          # Build with all modules (default active)
```

Maven wrapper is available: `./mvnw clean install`

Run `mvn process-sources -Pupdate-source-code-license` after adding new source
files — EPL-2.0 headers are enforced by `license-maven-plugin`.

### Maven profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Activates all submodules (active by default unless `skipModules=true`) |
| `sign-artifacts` | Signs artifacts with `sign-maven-plugin` for release |
| `release-dummy` | Deploys to local `/tmp/` directory for testing |
| `release-judong` | Deploys to `nexus.judo.technology` snapshot repository |
| `release-central` | Deploys to Maven Central via Sonatype OSSRH with staging |
| `generate-github-asciidoc-diagrams` | Generates HTML documentation from AsciiDoc with PlantUML diagrams |
| `update-source-code-license` | Updates EPL-2.0 license headers on all source files |

## Technology stack

- **Handlebars 4.1.2** — template engine for code generation (`.hbs` files)
- **Spring Expression Language (SpEL) 5.0.0** — evaluates the factory/path
  expressions in `psm-jaxrs.yaml`
- **Jackson 2.17.2** — JSON serialization (with `Jdk8Module` for `Optional`)
- **Guava 30.0** — collections and caching
- **Lombok 1.18.34** — provided scope
- **judo-meta-psm** — the input model
- **judo-generator-commons** — shared generator utilities and the
  `StaticMethodValueResolver` base class
- **judo-sdk-common / judo-dao-api / judo-dispatcher-api** — runtime SDK
  dependencies referenced by generated code
- Build/quality: `flatten-maven-plugin` for `${revision}`, JUnit Jupiter 5 via
  `maven-surefire-plugin` 3.5.1, JaCoCo 0.8.12, `sonar-maven-plugin`,
  `maven-bundle-plugin` 5.1.2, `license-maven-plugin`

**Required toolchain:** Java 21 JDK, Maven 3.9.4+.

## Template authoring contracts

1. **Template configuration is split across modules** — each module's
   `psm-jaxrs.yaml` declares only its own templates; the generator plugin loads
   all URIs and merges them. Adding a template means editing that module's
   descriptor, not a central one.
2. **All helper classes extend `StaticMethodValueResolver` and are annotated
   `@TemplateHelper`.** Templates call them with `#methodName(args)` syntax —
   that is the Handlebars ↔ SpEL integration.
3. `StoredVariableHelper` is annotated `@ContextAccessor` and holds template
   parameters in `ThreadLocal` variables.
4. `JavaNamespaceHelper` and `ObjectTypeHelper` use Guava `LoadingCache` for
   expensive lookups.
5. **Factory expressions** select the model elements to iterate (e.g.
   `#allTransferObject(#model)`); **path expressions** compute the output file
   location (e.g. `#namedElementApiParentPath(#self)`).
6. **Generated DTOs have different shapes per use case** — separate classes for
   response (with `@JsonInclude(NON_NULL)`), request, create-request, and
   range-request.
7. **Mapped transfer objects include metadata fields** — `__deleteable`,
   `__updateable`, `__entityType`, `__identifier`, `__signedIdentifier`,
   `__version`, `__referenceId`.
8. **`safeName()` in `JavaNamespaceHelper` handles Java reserved keywords** — it
   prefixes them with `_` so generated code still compiles.
9. **`OperationHelper` maps PSM behaviour types to JAX-RS paths** — LIST,
   EXPORT, CREATE_INSTANCE, VALIDATE_CREATE, REFRESH, UPDATE_INSTANCE,
   DELETE_INSTANCE, SET/UNSET/ADD/REMOVE_REFERENCE, GET_RANGE, GET_TEMPLATE,
   GET_PRINCIPAL, GET_METADATA, GET_UPLOAD_TOKEN.
10. **OSGi bundle packaging is mandatory** — every reactor module produces an
    OSGi bundle via `maven-bundle-plugin`.
11. **Package/path mismatch, deliberate:** the impl helper lives on disk under
    `.../generator/jackson/impl/` but its Java package is
    `hu.blackbelt.judo.psm.generator.jaxrs.impl`; the OSGi helper has the same
    disk-vs-package split. Trust the `package` declaration, not the path.
12. Optional generation switches: OpenAPI/Swagger annotations
    (`generateOpenApiAnnotations`) and Java `Optional` type wrapping.

## Code instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

## Git workflow

- **Main branch:** `develop`
- **Versioning:** CI-friendly with `${revision}`; snapshots use
  `major.minor.qualifier.date_commitId_branchName`
- **Branch naming:** `feature/JNG-NNN_summary`, `bugfix/JNG-NNN_summary`,
  `release/X.Y.Z`
- **Rule:** every commit must reference a JIRA ticket (`JNG-xxx`)

## Related documentation

- [README.md](README.md) — usage guide with Maven plugin configuration example
- [CONTRIBUTING.md](CONTRIBUTING.md) — development setup and contribution guidelines
- [.github/CIFLOW.md](.github/CIFLOW.md) — CI/CD workflow, branch and versioning details
- [judo-meta-psm](https://github.com/BlackBeltTechnology/judo-meta-psm) — PSM model and generator plugin documentation
- [judo-community CONTRIBUTING](https://github.com/BlackBeltTechnology/judo-community/blob/develop/CONTRIBUTING.adoc) — parent project contribution guide

<!-- dox-doctrine -->
## Documentation Update Protocol (WRITE discipline)

Per-directory `AGENTS.md` files form a tree. Each directory `AGENTS.md` is the
per-file record for the files in that directory. This module-root `AGENTS.md`
holds doctrine + architecture pointers only — never a per-file index.

**Keep the root lean.** This file loads into every agent turn — every byte costs
tokens on every turn. A verbose root file buries the rules the model must follow
(signal dilution) and measurably degrades adherence; a lean file keeps doctrine
salient. Default assumption: your update does NOT belong in the root — route it
by the table below.

**Route every doc update by kind:**

| Kind of update | Goes in |
|---|---|
| New file in a directory, or its per-file detail / change history | Nearest directory `AGENTS.md`. Add a `` | `<basename>` | <purpose> | `` row, path-alphabetical. |
| Data flow, protocol, architecture rationale | `docs/architecture.md` or a `docs/<topic>.md` |
| End-user / developer setup | `README.md` |
| Cross-cutting rule every agent needs every turn (rare) | this module-root `AGENTS.md` |

**Read before editing (chain walk).** Before editing a file, read the nearest
`AGENTS.md` chain root→leaf so you know the file's recorded purpose, contracts,
and change history. Do not edit blind.

**Update after editing (closeout pass).** After changing a file, update its row
in the nearest directory `AGENTS.md`: find the file's row, update its purpose in
place; if absent, add it in path-alphabetical order. New directory → scaffold
its `AGENTS.md`. One row per file. The purpose carries a one-line summary, key
exported symbols, contracts/invariants, and `See change: <id>` history.

**Row style (caveman).** Short declarative fragments. Drop articles. Subject →
verb → object, present tense. One fact per row. Prefer concrete tokens (paths,
symbols, env vars) over prose. Keep identifiers verbatim.

**Size rule — split an over-large directory `AGENTS.md` file-based.** pi
auto-injects a directory `AGENTS.md` on every turn when cwd sits at/below it, so
an over-large directory `AGENTS.md` is not supported. Split it file-based: a row
exceeding the length threshold promotes to a per-file `<File>.AGENTS.md`
sidecar carrying that file's full detail (including every `See change:`). The
sidecar is pull-only — its name is not `AGENTS.md`, so pi never auto-injects it
— yet it stays search-indexed (`agents` doc_type). The directory `AGENTS.md`
keeps a one-line summary plus a `→ see `<File>.AGENTS.md`` pointer. Rows within
the threshold stay verbatim (lossless).

## Finding docs (READ discipline)

`kb_*` tools are faster and cheaper than raw search — they return a one-line
purpose + key exports per file, not raw bytes. **This fires on the ACTION, not
the intent** — before you `grep`/`rg` for a symbol, `cat`/read a file to learn
what it does, or chase an import, the kb call goes first. It fires **even
mid-task when you already know the file**; knowing the file does not exempt you.
When your reflex is the left column, run the right column instead:

| You're about to… | Do this FIRST instead |
|---|---|
| `grep -rn "SymbolName" src/` — find where a fn / type / const lives | `kb_search --doc-type agents "SymbolName"` — tree indexes key exports per file |
| `grep -rn "feature\|topic" src/` — how does X work / where's X handled | `kb_search "feature topic"` |
| `cat` / read a file just to learn its purpose before editing | `kb agents <path>` — one-line purpose + exports + change history |
| chase imports / callers across files | `kb_neighbors <path\|heading>` |
| read one doc section in full | `kb_get <path> <section>` |

**Fall-through (explicit):** if the kb call returns nothing relevant, `rg` /
source read is allowed — then add the missing directory `AGENTS.md` row per the
WRITE discipline. kb does NOT replace grep; it goes first.
