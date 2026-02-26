# JUDO PSM Generator JAX-RS - Project Documentation

## Project Overview

**Repository:** BlackBeltTechnology/judo-psm-generator-jaxrs
**License:** Eclipse Public License 2.0 (EPL-2.0)
**Java Version:** 21
**Build System:** Maven 3.9.4 with OSGi bundle packaging (maven-bundle-plugin)

1. Generates complete Jakarta RESTful Web Services (JAX-RS) Java code from JUDO PSM (Platform-Specific Model) definitions
2. Produces four layers: API interfaces/DTOs, service implementations, OSGi component wrappers, and shared template fragments
3. Uses Handlebars templates with custom `@TemplateHelper` Java classes to transform model elements into type-safe Java source
4. Template-to-model mapping is configured declaratively in `psm-jaxrs.yaml` files using factory/path expressions
5. Supports optional OpenAPI/Swagger annotation generation and Java `Optional` type wrapping

## Directory Structure

```
judo-psm-generator-jaxrs/
├── pom.xml                              # Parent POM (Java 21, CI-friendly versioning)
├── logback-test.xml                     # Test logging configuration
├── mvnw / mvnw.cmd                      # Maven wrapper
├── judo-psm-generator-jaxrs-common/     # Shared template fragments
├── judo-psm-generator-jaxrs-api/        # API layer: helpers + templates
├── judo-psm-generator-jaxrs-impl/       # Implementation layer
├── judo-psm-generator-jaxrs-osgi/       # OSGi component layer
├── judo-psm-generator-jaxrs-test/       # Serialization tests
├── .github/workflows/                   # CI/CD pipelines
└── .github/CIFLOW.md                    # CI flow documentation
```

## Core Modules

### Template & Helper Modules

| Module | Type | Purpose |
|--------|------|---------|
| `judo-psm-generator-jaxrs-common/` | OSGi Bundle | Shared Handlebars fragments (e.g., `fragment.header.hbs` for license headers). Contains empty `psm-jaxrs.yaml`. |
| `judo-psm-generator-jaxrs-api/` | OSGi Bundle | API layer generation: REST service interfaces, request/response/create-request DTOs, enumerations, query customizers, range objects, error/detail classes, application configuration. Contains 8 `@TemplateHelper` classes and 10+ `.hbs` templates. |
| `judo-psm-generator-jaxrs-impl/` | OSGi Bundle | Implementation layer: concrete service implementations (`*Impl`), application config implementations, REST utility classes. Contains `JavaImplHelper`. |
| `judo-psm-generator-jaxrs-osgi/` | OSGi Bundle | OSGi wrapper layer: component wrappers (`*Component`) with `@Component` declarations for service registration. Contains `JavaOsgiHelper`. |

### Test Module

| Module | Type | Purpose |
|--------|------|---------|
| `judo-psm-generator-jaxrs-test/` | OSGi Bundle | Unit tests for Jackson serialization with `Optional` types. Validates DTO serialization contracts using `ObjectMapper` + `Jdk8Module`. |

## Technology Stack

### Core Technologies
- **Handlebars 4.1.2** — Template engine for code generation (`.hbs` files)
- **Spring Expression Language (SpEL) 5.0.0** — Expression evaluation in `psm-jaxrs.yaml` factory/path expressions
- **Jackson 2.17.2** — JSON serialization (with `Jdk8Module` for `Optional` support)
- **Guava 30.0** — Utility library (collections, caching)
- **Lombok 1.18.34** — Boilerplate reduction (provided scope)
- **judo-meta-psm** — PSM model definitions (the input model)
- **judo-generator-commons** — Shared generator utilities and `StaticMethodValueResolver` base class
- **judo-sdk-common / judo-dao-api / judo-dispatcher-api** — Runtime SDK dependencies referenced in generated code

### Build & Quality
- **Maven 3.9.4** with `flatten-maven-plugin` for CI-friendly `${revision}` versioning
- **JUnit Jupiter 5** (via `maven-surefire-plugin` 3.5.1)
- **JaCoCo 0.8.12** for code coverage
- **SonarQube** integration via `sonar-maven-plugin`
- **maven-bundle-plugin 5.1.2** for OSGi bundle packaging
- **license-maven-plugin** for EPL-2.0 header enforcement

## Build Commands

```bash
mvn clean install                    # Full build with tests
mvn clean test                       # Run tests only
mvn test -Dtest=SerializationTest    # Run a single test
mvn clean install -Pmodules          # Build with all modules (default active)
```

Maven wrapper is available: `./mvnw clean install`

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Activates all submodules (active by default unless `skipModules=true`) |
| `sign-artifacts` | Signs artifacts with `sign-maven-plugin` for release |
| `release-dummy` | Deploys to local `/tmp/` directory for testing |
| `release-judong` | Deploys to `nexus.judo.technology` snapshot repository |
| `release-central` | Deploys to Maven Central via Sonatype OSSRH with staging |
| `generate-github-asciidoc-diagrams` | Generates HTML documentation from AsciiDoc with PlantUML diagrams |
| `update-source-code-license` | Updates EPL-2.0 license headers on all source files |

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Parent POM: Java 21, dependency versions, plugin management, profiles |
| `*/psm-jaxrs.yaml` | Template configuration: maps Handlebars templates to model elements via factory/path expressions |
| `*/src/main/resources/rest/**/*.hbs` | Handlebars templates that produce generated Java code |
| `logback-test.xml` | Logback configuration for test execution |
| `.github/workflows/build.yml` | Main CI pipeline: build, deploy, tag, release |

## Development Environment

**Required:**
- Java 21 JDK
- Maven 3.9.4+

**Key Architecture Concepts:**
- All helper classes extend `StaticMethodValueResolver` and are annotated with `@TemplateHelper`
- `StoredVariableHelper` is annotated with `@ContextAccessor` and stores template parameters in `ThreadLocal` variables
- `JavaNamespaceHelper` and `ObjectTypeHelper` use Guava `LoadingCache` for expensive lookups
- Factory expressions in `psm-jaxrs.yaml` use SpEL to call helper methods (e.g., `#allTransferObject(#model)`)
- Path expressions determine output file locations using helper methods (e.g., `#namedElementApiParentPath(#self)`)
- The `impl` module helper is in package `hu.blackbelt.judo.psm.generator.jaxrs.impl` (file path says `jackson` but Java package is `jaxrs`)
- The `osgi` module helper is in package `hu.blackbelt.judo.psm.generator.jaxrs.osgi` (same note)

## Git Workflow

- **Main Branch:** `develop`
- **Versioning:** CI-friendly with `${revision}` property; snapshots use `major.minor.qualifier.date_commitId_branchName` format
- **Branch naming:** `feature/JNG-NNN_summary`, `bugfix/JNG-NNN_summary`, `release/X.Y.Z`
- **Rule:** Every commit must reference a JIRA ticket (`JNG-xxx`)

## Important Notes

1. **Template configuration is split across modules** — each module's `psm-jaxrs.yaml` defines only its own templates; the generator plugin loads all URIs and merges them
2. **Helper methods are called from templates using `#methodName(args)` syntax** — this is the Handlebars integration with `StaticMethodValueResolver`
3. **Generated DTOs have different shapes per use case** — separate classes for response (with `@JsonInclude(NON_NULL)`), request, create-request, and range-request
4. **Mapped transfer objects include metadata fields** — `__deleteable`, `__updateable`, `__entityType`, `__identifier`, `__signedIdentifier`, `__version`, `__referenceId`
5. **The `safeName()` method in `JavaNamespaceHelper` handles Java reserved keywords** — it prefixes them with `_` to avoid compilation errors in generated code
6. **Operation helper maps PSM behaviour types to JAX-RS paths** — LIST, EXPORT, CREATE_INSTANCE, VALIDATE_CREATE, REFRESH, UPDATE_INSTANCE, DELETE_INSTANCE, SET/UNSET/ADD/REMOVE_REFERENCE, GET_RANGE, GET_TEMPLATE, GET_PRINCIPAL, GET_METADATA, GET_UPLOAD_TOKEN
7. **OSGi bundle packaging is mandatory** — all modules produce OSGi bundles via `maven-bundle-plugin`
8. **License headers are auto-enforced** — run `mvn process-sources -Pupdate-source-code-license` after adding new source files

## Related Documentation

- [README.md](README.md) — Usage guide with Maven plugin configuration example
- [CONTRIBUTING.md](CONTRIBUTING.md) — Development setup and contribution guidelines
- [.github/CIFLOW.md](.github/CIFLOW.md) — CI/CD workflow documentation with branch and versioning details
- [judo-meta-psm](https://github.com/BlackBeltTechnology/judo-meta-psm) — PSM model and generator plugin documentation
- [judo-community CONTRIBUTING](https://github.com/BlackBeltTechnology/judo-community/blob/develop/CONTRIBUTING.adoc) — Parent project contribution guide
