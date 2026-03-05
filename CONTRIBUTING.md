# Contributing to JUDO PSM Generator JAX-RS

## Development Environment Setup

Your development environment must meet the requirements described in the parent project's [CONTRIBUTING guide](https://github.com/BlackBeltTechnology/judo-community/blob/develop/CONTRIBUTING.adoc). In summary:

- **Java 21** JDK
- **Maven 3.9.4+** (or use the included `./mvnw` wrapper)

## Code Structure

This project is a multi-module Maven project that generates Jakarta RESTful Web Services code from PSM models. The modules are:

| Module | Contains |
|--------|----------|
| `judo-psm-generator-jaxrs-common` | Shared Handlebars template fragments |
| `judo-psm-generator-jaxrs-api` | API templates and all `@TemplateHelper` classes for interfaces, DTOs, enums |
| `judo-psm-generator-jaxrs-impl` | Implementation templates and helpers |
| `judo-psm-generator-jaxrs-osgi` | OSGi component templates and helpers |
| `judo-psm-generator-jaxrs-test` | Unit tests (Jackson serialization with `Optional` types) |

## Commands

### Run Tests

```sh
mvn clean test
```

### Run Full Build

```sh
mvn clean install
```

### Run a Single Test

```sh
mvn test -Dtest=SerializationTest
```

## Submitting an Issue

Before filing a new issue, search the [issue tracker](https://github.com/BlackBeltTechnology/judo-psm-generator-jaxrs/issues) first — your problem may already have been reported or resolved.

When reporting a bug, include:

- Output of `java -version` and `mvn -version`
- Relevant `pom.xml` or `.flattened-pom.xml`
- A **minimal reproduction** — the smallest possible project or configuration that demonstrates the problem

A minimal reproduction lets maintainers quickly confirm the bug and ensures the right problem gets fixed. We will ask for one before investigating.

File new issues using the [issue form](https://github.com/BlackBeltTechnology/judo-psm-generator-jaxrs/issues/new/choose).

## Submitting a Pull Request

This project uses [GitHub's standard forking model](https://guides.github.com/activities/forking/). Fork the repository and submit pull requests from your fork.

> **Important:** Every commit and pull request must reference a JIRA ticket number (e.g., `JNG-123`). There is no commit without a ticket number.
