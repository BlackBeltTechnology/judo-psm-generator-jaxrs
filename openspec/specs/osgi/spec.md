# osgi Specification

## Purpose
Generates OSGi component wrappers for REST services, enabling JAX-RS services to be registered as OSGi Declarative Services components with proper lifecycle management.

## Architecture

### Helper Class
- **JavaOsgiHelper** (extends `StaticMethodValueResolver`, `@TemplateHelper`) — Provides OSGi-specific naming: `osgiClassName()` adds `Component` suffix, `applicationOsgiClassName()` adds `Component` suffix to application config, `getOsgiPrefixLocal()` retrieves the `osgiPrefix` template parameter from `ThreadLocal`, `namedElementOsgiApplicationPath()` and `namedElementOsgiApplicationName()` generate OSGi application metadata

### Templates (`psm-jaxrs.yaml`)
- `application.config.osgi.java.hbs` — OSGi component wrapping application configuration (one per access point actor)
- `rest.service.osgi.java.hbs` — OSGi component wrapping REST service (one per exposed transfer object with operations)

### Dependencies
- Depends on `judo-psm-generator-jaxrs-api` for interface definitions and API helper classes
- Uses `JavaApiHelper` and `JavaNamespaceHelper` from the API module

## Requirements

### Requirement: OSGi components SHALL be generated for all REST service implementations
An OSGi component wrapper SHALL be generated for each REST service, registering it as a Declarative Services component.

#### Scenario: OSGi component for Order service
- **GIVEN** an API module defines `OrderRestService` and impl defines `OrderRestServiceImpl`
- **WHEN** the OSGi module runs code generation
- **THEN** `OrderRestServiceComponent` is generated with `@Component` annotation
- **THEN** the component is in the package defined by the `osgiPrefix` template parameter

### Requirement: OSGi component classes SHALL use Component suffix naming convention
`JavaOsgiHelper.osgiClassName()` SHALL append `Component` to the base class name from the API layer.

#### Scenario: Component class naming
- **GIVEN** an API-layer class named `CustomerRestService`
- **WHEN** `osgiClassName()` is called
- **THEN** the result is `CustomerRestServiceComponent`

### Requirement: Application configuration OSGi component SHALL be generated per access point actor
One OSGi application config component SHALL be generated for each access point actor, wrapping the corresponding implementation.

#### Scenario: Application config OSGi component
- **GIVEN** an access point actor `AdminActor` in the PSM model
- **WHEN** code generation runs
- **THEN** an application config component class is generated with `Component` suffix
- **THEN** `namedElementOsgiApplicationPath()` provides the correct application path
- **THEN** `namedElementOsgiApplicationName()` provides the component name
