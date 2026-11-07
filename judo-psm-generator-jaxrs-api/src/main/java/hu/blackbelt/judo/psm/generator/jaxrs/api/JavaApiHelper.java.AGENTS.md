# `JavaApiHelper.java`

`@TemplateHelper` extending `StaticMethodValueResolver`. Central name factory for the API artifact:
every generated package, class name and Java type on the API side resolves through here.

## Exports

- Packages/paths: `apiPackageName()`, `namespaceElementApiFqPath`, `namedElementApiPackageName`,
  `namedElementApiFqName`, `namedElementApiParentPath`, `namedElementApiRestPackageName`,
  `namedElementApiRestParentPath`, `namedElementApiRestFqName`.
- Classes: `className(NamedElement)` (capitalized `safeName`), `firstToLower`,
  `applicationClassName` / `applicationFqName` — `<Name>ApplicationConfig`.
- Enums: `enumFqName`, `enumPackageName`, `enumName`.
- Query customizers: `queryCustomizerPackageName`, `queryCustomizerName`.
- `classFqName(TransferObjectType)` — appends `Parameter` when the type carries the
  `ParameterObject` annotation.
- Model defaults: `apiDefaultPathName(Model)`, `apiDefaultPackageName(Model)` — both lowercase the
  model name.
- Constant `REST` = `rest`, the fixed sub-package for generated JAX-RS interfaces.

## `primitiveDataTypeDefinition(Primitive)`

PSM primitive → Java type mapping. `StringType` and `PasswordType` both map to `java.lang.String`;
`BooleanType`→`Boolean`; `DateType`→`java.time.LocalDate`; `TimestampType`→`LocalDateTime`;
`TimeType`→`LocalTime`; `BinaryType`→`hu.blackbelt.judo.dispatcher.api.FileType`;
`EnumerationType`→`enumFqName`.

`NumericType` widths are precision/scale driven:

- integer: precision 1–9 → `Integer`, 10–19 → `Long`, else `java.math.BigDecimal`
- decimal: precision 1–7 & scale ≤4 → `Float`, 8–15 & scale ≤4 → `Double`, else `BigDecimal`

## Contracts

- Unmatched types fall through to `java.lang.Object` — a new PSM primitive silently degrades to
  `Object` instead of failing the build.
- Every package name is prefixed from ThreadLocal `apiPrefix` via `StoredVariableHelper`. A
  generator thread that never calls `StoredVariableHelper.bindContext` emits unprefixed packages.
- `namespaceElementApiFqPath` runs `replaceAll(".", "/")` with an unescaped `.` regex — the prefix
  segment becomes all slashes. Depended on by existing templates; do not "fix" without regenerating.
