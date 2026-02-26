# api Specification

## Purpose
Generates the API layer of JAX-RS services from PSM models: REST service interfaces, request/response/create-request DTOs, enumerations, query customizers, range request objects, error/detail classes, and application configuration classes.

## Architecture

### Helper Classes (all extend `StaticMethodValueResolver`, annotated `@TemplateHelper`)
- **ModelHelper** — PSM model navigation: queries for transfer objects, enums, ranges, access points, query customizers
- **StoredVariableHelper** (`@ContextAccessor`) — Thread-local storage for template parameters (`apiPrefix`, `generateOptionalTypes`, `generateOpenApiAnnotations`, etc.)
- **JavaApiHelper** — Package/class name generation, file path expressions, primitive type mapping (PSM → Java)
- **JavaNamespaceHelper** — Namespace resolution with Guava `LoadingCache`, safe name conversion for Java reserved keywords
- **ObjectTypeHelper** — Entity lookup with caching, actor type checking, DTO class name generation for response/request/create-request/range-request variants
- **OperationHelper** — Maps PSM operation behaviour types (LIST, CREATE_INSTANCE, DELETE_INSTANCE, etc.) to JAX-RS paths
- **AttributeHelper** — Attribute type definitions with Optional wrapping, annotation checks (QueryWithoutParameter, DefaultValue, DataProperty)
- **RelationHelper** — Relation type definitions with List/Optional wrappers based on cardinality, embedded/mapped relation filtering

### Templates (`psm-jaxrs.yaml` configuration)
- `application.config.java.hbs` — JAX-RS application config (one per access point actor)
- `rest.service.java.hbs` — REST service interface (one per exposed transfer object with operations)
- `dtoForResponse.java.hbs` — Response DTO with `@JsonInclude(NON_NULL)`
- `dtoForRequest.java.hbs` — Request DTO
- `dtoForCreateRequest.java.hbs` — Create request DTO
- `dtoForRangeRequest.java.hbs` — Range request DTO
- `enum.java.hbs` — Enumeration class
- `dtoForQueryCustomizer.java.hbs` — Query customizer DTO
- `error.java.hbs` — Error response class
- `detail.java.hbs` — Detail/metadata class

## Requirements

### Requirement: REST service interfaces SHALL be generated for all exposed transfer objects with operations
The generator SHALL produce a REST service interface for every transfer object exposed through an access point that has operations.

#### Scenario: Transfer object with CRUD operations
- **GIVEN** a PSM model with a transfer object `Order` exposed through an access point actor
- **WHEN** `Order` has LIST, CREATE_INSTANCE, and DELETE_INSTANCE operations
- **THEN** a REST service interface is generated with JAX-RS annotated methods for each operation
- **THEN** the factory expression `#allExposedTransferObjectWithOperation(#model)` includes `Order`

### Requirement: Separate DTO classes SHALL be generated for each use case
The generator SHALL produce distinct DTO classes for response, request, create-request, and range-request contexts.

#### Scenario: Transfer object DTO variants
- **GIVEN** a PSM transfer object `Customer`
- **WHEN** code generation runs
- **THEN** four DTO classes are generated: `CustomerForResponse`, `CustomerForRequest`, `CustomerForCreateRequest`, `CustomerForRangeRequest`
- **THEN** the response DTO includes `@JsonInclude(NON_NULL)` annotation

### Requirement: Mapped transfer objects SHALL include metadata fields
DTOs for mapped transfer objects SHALL include entity tracking metadata fields.

#### Scenario: Mapped transfer object response DTO
- **GIVEN** a mapped transfer object (where `ModelHelper.isMapped()` returns `true`)
- **WHEN** the response DTO is generated
- **THEN** it includes fields: `__deleteable`, `__updateable`, `__entityType`, `__identifier`, `__signedIdentifier`, `__version`, `__referenceId`

### Requirement: Java reserved keywords SHALL be escaped in generated names
`JavaNamespaceHelper.safeName()` SHALL prefix Java reserved keywords to prevent compilation errors.

#### Scenario: Model element named with reserved keyword
- **GIVEN** a PSM model element with name `class`
- **WHEN** `safeName("class")` is called
- **THEN** the returned name is `_class`

### Requirement: Optional type wrapping SHALL be controlled by template parameter
When `generateOptionalTypes` is `true`, nullable attributes SHALL be wrapped in `java.util.Optional`.

#### Scenario: Optional type generation enabled
- **GIVEN** `generateOptionalTypes` template parameter is `true`
- **WHEN** an attribute is nullable and eligible for Optional wrapping
- **THEN** `AttributeHelper.attributeTargetTypeDefinition()` returns `Optional<Type>` instead of `Type`

### Requirement: OpenAPI annotations SHALL be generated when configured
When `generateOpenApiAnnotations` is `true`, REST service interfaces and DTOs SHALL include Swagger/OpenAPI annotations.

#### Scenario: OpenAPI annotations enabled
- **GIVEN** `generateOpenApiAnnotations` is `true` and `baseUrl`, `authenticationUrl`, `specificationVersionNumber` are set
- **WHEN** REST service interfaces are generated
- **THEN** the generated code includes OpenAPI `@Operation`, `@Tag`, and security scheme annotations

### Requirement: Operation helper SHALL map all PSM behaviour types to JAX-RS paths
`OperationHelper.getJAXRSPath()` SHALL return correct REST paths for all supported behaviour types.

#### Scenario: LIST operation path
- **GIVEN** a transfer operation with behaviour type LIST
- **WHEN** `toJAXRSPath()` is called
- **THEN** a valid JAX-RS path string is returned for list operations

#### Scenario: CREATE_INSTANCE operation path
- **GIVEN** a transfer operation with behaviour type CREATE_INSTANCE
- **WHEN** `toJAXRSPath()` is called
- **THEN** a valid JAX-RS path string is returned for create operations

### Requirement: Primitive PSM types SHALL map to correct Java types
`JavaApiHelper.primitiveDataTypeDefinition()` SHALL map all PSM primitive types to their Java equivalents.

#### Scenario: PSM StringType mapping
- **GIVEN** a PSM attribute with `StringType` data type
- **WHEN** `primitiveDataTypeDefinition()` is called
- **THEN** the Java type `String` is returned

#### Scenario: PSM NumericType mapping
- **GIVEN** a PSM attribute with `NumericType` data type
- **WHEN** `primitiveDataTypeDefinition()` is called
- **THEN** an appropriate Java numeric type is returned (e.g., `java.math.BigDecimal`)
