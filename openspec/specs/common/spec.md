# common Specification

## Purpose
Provides shared Handlebars template fragments used by all other generator modules, ensuring consistent headers and common code patterns across generated Java files.

## Architecture
- `fragment.header.hbs` — shared header fragment included in every generated Java file (license header, package declaration)
- `psm-jaxrs.yaml` — empty configuration (no standalone templates; fragments are included by other modules' templates)

## Requirements

### Requirement: Shared header fragment SHALL be available to all modules
All generator modules SHALL be able to include the `fragment.header.hbs` fragment in their templates to produce consistent file headers.

#### Scenario: API template includes header fragment
- **GIVEN** a Handlebars template in the API module
- **WHEN** the template uses `{{> fragment.header}}` partial inclusion
- **THEN** the generated Java file includes the standard license header and package declaration

### Requirement: Common module SHALL define no standalone templates
The `psm-jaxrs.yaml` in the common module SHALL be empty, defining no template-to-model mappings of its own.

#### Scenario: Common module loaded by generator
- **WHEN** the generator plugin loads the common module URI
- **THEN** no Java files are generated directly from the common module
- **THEN** only fragment partials are registered for use by other modules
