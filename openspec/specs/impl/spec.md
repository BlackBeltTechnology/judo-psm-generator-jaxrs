# impl Specification

## Purpose
Generates the implementation layer of JAX-RS services: concrete service implementations, application configuration implementations, and REST utility classes that delegate to the JUDO dispatcher.

## Architecture

### Helper Class
- **JavaImplHelper** (extends `StaticMethodValueResolver`, `@TemplateHelper`) — Provides implementation-specific naming: `implClassName()` adds `Impl` suffix, `applicationImplClassName()` adds `Impl` suffix to application config, `getImplPrefixLocal()` retrieves the `implPrefix` template parameter from `ThreadLocal`

### Templates (`psm-jaxrs.yaml`)
- `application.config.impl.java.hbs` — Implementation of JAX-RS application configuration (one per access point actor)
- `rest.service.impl.java.hbs` — Concrete REST service implementation (one per exposed transfer object with operations)
- `rest.utils.java.hbs` — REST utility class with shared helper methods

### Dependencies
- Depends on `judo-psm-generator-jaxrs-api` for interface definitions and API helper classes
- Uses `JavaApiHelper` and `JavaNamespaceHelper` from the API module

## Requirements

### Requirement: Service implementations SHALL be generated for all exposed transfer objects with operations
A concrete implementation class SHALL be generated for each REST service interface defined in the API module.

#### Scenario: Implementation class for Order service
- **GIVEN** an API module generates `OrderRestService` interface
- **WHEN** the impl module runs code generation
- **THEN** `OrderRestServiceImpl` is generated implementing the interface
- **THEN** the class is in the package defined by the `implPrefix` template parameter

### Requirement: Implementation classes SHALL use Impl suffix naming convention
`JavaImplHelper.implClassName()` SHALL append `Impl` to the base class name from the API layer.

#### Scenario: Impl class naming
- **GIVEN** an API-layer class named `CustomerRestService`
- **WHEN** `implClassName()` is called
- **THEN** the result is `CustomerRestServiceImpl`

### Requirement: Application configuration implementation SHALL be generated per access point actor
One implementation application config class SHALL be generated for each access point actor in the model.

#### Scenario: Application config implementation
- **GIVEN** an access point actor `AdminActor` in the PSM model
- **WHEN** code generation runs
- **THEN** an application config implementation class is generated with `Impl` suffix
- **THEN** the path is resolved using `namedElementImplRestParentPath()`

### Requirement: REST utility class SHALL be generated
A shared utility class SHALL be generated to provide common REST helper methods.

#### Scenario: Utils class generation
- **GIVEN** the PSM model has at least one exposed transfer object with operations
- **WHEN** code generation runs
- **THEN** a REST utility class is generated in the implementation package
