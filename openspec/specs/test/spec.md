# test Specification

## Purpose
Validates that generated DTOs serialize and deserialize correctly with Jackson, particularly for `java.util.Optional` field handling via `Jdk8Module`.

## Architecture
- **SerializationTest** — JUnit 5 test class using `ObjectMapper` configured with `Jdk8Module` to test serialization/deserialization round-trips for DTOs with `Optional` fields

## Requirements

### Requirement: Empty DTOs SHALL serialize to empty JSON objects
When a DTO has no fields set (all `Optional` fields uninitialized), Jackson SHALL produce `{}`.

#### Scenario: Serialize DTO with no fields set
- **GIVEN** a DTO instance with an `Optional<String> name` field that is `null` (unset)
- **WHEN** serialized with `ObjectMapper` configured with `Jdk8Module`
- **THEN** the output is `{}`

### Requirement: Optional.empty() SHALL serialize to explicit null
When a DTO field is set to `Optional.empty()`, Jackson SHALL produce `{"fieldName":null}`.

#### Scenario: Serialize DTO with Optional.empty() field
- **GIVEN** a DTO instance where `name` is set to `Optional.empty()`
- **WHEN** serialized with `ObjectMapper` + `Jdk8Module`
- **THEN** the output is `{"name":null}`

### Requirement: Optional.of(value) SHALL serialize to the value
When a DTO field is set to `Optional.of("Test")`, Jackson SHALL produce `{"fieldName":"Test"}`.

#### Scenario: Serialize DTO with Optional value
- **GIVEN** a DTO instance where `name` is set to `Optional.of("Test")`
- **WHEN** serialized with `ObjectMapper` + `Jdk8Module`
- **THEN** the output is `{"name":"Test"}`

### Requirement: Missing JSON fields SHALL deserialize to null object reference
When a JSON object does not contain a field, the corresponding DTO field SHALL be `null` (not `Optional.empty()`).

#### Scenario: Deserialize JSON missing the field
- **GIVEN** JSON input `{}`
- **WHEN** deserialized to the DTO class
- **THEN** the `name` field is `null`

### Requirement: Explicit null in JSON SHALL deserialize to Optional.empty()
When a JSON field is explicitly `null`, the corresponding DTO field SHALL be `Optional.empty()`.

#### Scenario: Deserialize JSON with null field
- **GIVEN** JSON input `{"name":null}`
- **WHEN** deserialized to the DTO class
- **THEN** the `name` field is `Optional.empty()`

### Requirement: JSON value SHALL deserialize to Optional.of(value)
When a JSON field has a value, the corresponding DTO field SHALL be `Optional.of(value)`.

#### Scenario: Deserialize JSON with string value
- **GIVEN** JSON input `{"name":"Test"}`
- **WHEN** deserialized to the DTO class
- **THEN** the `name` field is `Optional.of("Test")`
