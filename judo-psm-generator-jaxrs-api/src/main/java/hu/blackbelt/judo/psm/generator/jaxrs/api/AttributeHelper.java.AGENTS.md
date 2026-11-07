# `AttributeHelper.java`

`@TemplateHelper` extending `StaticMethodValueResolver`. Maps a PSM `TransferAttribute` onto the
Java type emitted into DTOs, and decides which DTO flavour carries it.

## Exports

- `attributeTargetTypeDefinition(TransferAttribute)` — bare type, wrapped `java.util.Optional<…>`
  when `isOptionalAttribute` holds.
- `attributeTargetBareTypeDefinition(TransferAttribute)` — delegates to
  `JavaApiHelper.primitiveDataTypeDefinition(attr.getDataType())`.
- `isOptionalAttribute` — true only when context variable `generateOptionalTypes` is true AND the
  attribute is not required.
- `isQueryOrDefaultValueOrDataPropertyAttribute`, `isRequestAttribute`, `isResponseAttribute` —
  three progressively narrower inclusion predicates. Request admits
  `QueryWithoutParameter`-annotated bindings; response does NOT.
- `isParametrizedAttribute` — binding is a `PrimitiveAccessor` whose getter expression declares a
  `parameterType`.
- `hasQueryWithoutParamAnnotation`, `hasDefaultValueAnnotation` (checks binding annotation
  `DefaultValue` OR attribute annotation `TransferObjectAttributeWithDefaultValue`),
  `isDefaultValueAttribute`, `isDataProperty` (binding is a `DataProperty`).
- `hasAnnotation(EList<Annotation>, String name)` — name-equality scan, reused by callers.

## Contracts

- `isDefaultValueAttribute` matches on the name substring `_default_`, not on any annotation.
  Renaming that PSM naming convention silently drops the attribute from generated request DTOs.
- All predicates null-guard `getBinding()`; an unbound attribute answers `false` everywhere and is
  emitted as a plain data field.
