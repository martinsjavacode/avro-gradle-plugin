# Avro Gradle Plugin

[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/io/github/martinsjavacode/avro-gradle-plugin/maven-metadata.xml.svg)](https://plugins.gradle.org/plugin/io.github.martinsjavacode.avro-gradle-plugin)
[![Release Gradle Plugin](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/release.yaml/badge.svg)](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/release.yaml)
[![Tests](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/test.yaml/badge.svg)](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/test.yaml)
[![CodeQL Analysis](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/codeql-analysis.yaml/badge.svg)](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/codeql-analysis.yaml)
[![Dependabot Updates](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/dependabot/dependabot-updates/badge.svg)](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/dependabot/dependabot-updates)
[![Code Coverage](https://codecov.io/gh/martinsjavacode/avro-gradle-plugin/branch/main/graph/badge.svg?token=2Q0X1G3J9E)](https://codecov.io/gh/martinsjavacode/avro-gradle-plugin)
![GitHub last commit](https://img.shields.io/github/last-commit/martinsjavacode/avro-gradle-plugin)




The **Avro Gradle Plugin** simplifies the process of generating Java classes from Avro schema files. With a focus on flexibility and ease of use, this plugin integrates seamlessly into the Gradle build process, offering extensive customization options for developers.

---

## **Table of Contents**
- [Features](#features)
- [Requirements](#requirements)
- [Configuration Options](#configuration-options)
- [Usage Example](#usage-example)
- [Migration from 1.x](#migration-from-1x)
- [Contributing](#contributing)
- [Issues](#issues)
- [License](#license)

---

## **Features**
- **Automatic Java Class Generation**: Generate Java classes directly from Avro schema files.
- **Multiple Schema Formats**: Support for `.avsc` (JSON), `.avpr` (Protocol), and `.avdl` (Avro IDL) files.
- **Configuration Cache Compatible**: Fully compatible with Gradle's configuration cache.
- **Incremental Build Cache**: Gradle build cache support for faster incremental builds.
- **Schema Validation**: Dedicated task to validate schemas before generation.
- **HTML Reports**: Automatic generation of detailed HTML reports with build statistics.
- **Customizable Directories**: Specify source and output directories for Avro schema files and generated classes.
- **Field Visibility Control**: Configure fields as `PUBLIC` or `PRIVATE`.
- **String Type Selection**: Choose between `String`, `CharSequence`, or `Utf8` for string representations.
- **Optional Getters**: Enable or disable optional getter methods in the generated classes.
- **Null-Safe Annotations**: Optionally add null-safe annotations to generated classes.
- **Decimal Logical Type Support**: Enable support for decimal logical types in Avro schemas.
- **Enhanced Error Handling**: Detailed error messages with file-specific context.
- **Lazy Configuration**: Uses Gradle's `Property<T>` API for optimal performance.
- **Automatic Task Creation**: Automatically adds tasks to your Gradle workflow.

---

## **Requirements**
- **Java**: Version 21 or higher.
- **Gradle**: Version 8.0 or higher.
- **Apache Avro**: Version 1.12.0 or higher.

---

## **Configuration Options**

Define configuration options in the `avro` block within your project's `build.gradle.kts` file. All options use Gradle's `Property<T>` API:

| **Option**                | **Description**                                                                     | **Default Value**            | **Allowed Values**           |
|---------------------------|-------------------------------------------------------------------------------------|-----------------------------|-----------------------------|
| `sourceDir`               | Directory containing Avro schema files (`*.avsc`, `*.avpr`, `*.avdl`).              | `src/main/resources/avro`   | Custom directory path       |
| `outputDir`               | Directory to save generated Java classes (relative to build dir).                   | `generated/java`            | Custom directory path       |
| `fieldVisibility`         | Visibility of generated fields.                                                     | `PUBLIC`                    | `PUBLIC`, `PRIVATE`         |
| `stringType`              | String representation to use in generated classes.                                  | `String`                    | `String`, `CharSequence`, `Utf8` |
| `optionalGetters`         | Enables optional getter methods for generated fields.                               | `false`                     | `true`, `false`             |
| `useDecimalLogical`       | Enables support for decimal logical types in Avro schemas.                          | `false`                     | `true`, `false`             |
| `createNullSafeAnnotations` | Adds null-safe annotations to generated classes.                                   | `false`                     | `true`, `false`             |
| `validateBeforeGenerate`  | Validates schemas before generation.                                                | `true`                      | `true`, `false`             |

---

## **Usage Example**

Add the plugin to your `build.gradle.kts` file:

```kotlin
plugins {
    id("io.github.martinsjavacode.avro-gradle-plugin") version "2.0.0"
}

avro {
    sourceDir.set("src/main/avro")
    outputDir.set("generated-sources/avro")
    fieldVisibility.set("PRIVATE")
    stringType.set("CharSequence")
    optionalGetters.set(true)
}
```

Run the task to generate Avro classes:
```bash
./gradlew generateAvroClasses
```

Or validate schemas only:
```bash
./gradlew validateAvroSchemas
```

Or build the project to include Avro generation:
```bash
./gradlew build
```

---

## **Migration from 1.x**

Version 2.0.0 introduces breaking changes in the DSL syntax. The `avro {}` block now uses Gradle's `Property<T>` API:

```kotlin
// Before (1.x)
avro {
    sourceDir = "src/main/avro"
    fieldVisibility = "PRIVATE"
    optionalGetters = true
}

// After (2.x)
avro {
    sourceDir.set("src/main/avro")
    fieldVisibility.set("PRIVATE")
    optionalGetters.set(true)
}
```

This change enables full compatibility with Gradle's configuration cache and lazy task configuration.

---

## **Contributing**
Contributions are welcome! To contribute:
1.  Fork the repository.
2.  Create a feature branch:
    ```bash
    git checkout -b feature/new-feature
    ```
3. Commit your changes:
    ```bash
    git commit -m "Add new feature"
    ```
4. Push your branch:
    ```bash
    git push origin feature/new-feature
    ```
5. Open a Pull Request on GitHub.

---

## **Issues**
If you encounter any issues or have feature requests, please open an issue in the [GitHub repository](https://github.com/martinsjavacode/avro-gradle-plugin/issues).

---

## **License**
This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for full details.
