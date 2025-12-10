# Avro Gradle Plugin

[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/io/github/martinsjavacode/avro-gradle-plugin/maven-metadata.xml.svg)](https://plugins.gradle.org/plugin/io.github.martinsjavacode.avro-gradle-plugin)
[![Release Gradle Plugin](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/release.yaml/badge.svg)](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/release.yaml)
[![Tests](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/test.yaml/badge.svg)](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/test.yaml)
[![CodeQL Analysis](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/codeql-analysis.yaml/badge.svg)](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/codeql-analysis.yaml)
[![Dependabot Updates](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/dependabot/dependabot-updates/badge.svg)](https://github.com/martinsjavacode/avro-gradle-plugin/actions/workflows/dependabot/dependabot-updates)
[![Code Coverage](https://codecov.io/gh/martinsjavacode/avro-gradle-plugin/branch/main/graph/badge.svg?token=2Q0X1G3J9E)](https://codecov.io/gh/martinsjavacode/avro-gradle-plugin)
![GitHub last commit](https://img.shields.io/github/last-commit/martinsjavacode/avro-gradle-plugin)




The **Avro Gradle Plugin** simplifies the process of generating Java classes from Avro schema files (`*.avsc`). With a focus on flexibility and ease of use, this plugin integrates seamlessly into the Gradle build process, offering extensive customization options for developers.

---

## **Table of Contents**
- [Features](#features)
- [Requirements](#requirements)
- [Configuration Options](#configuration-options)
- [Usage Example](#usage-example)
- [Contributing](#contributing)
- [Authors](#authors)
- [Issues](#issues)
- [License](#license)

---

## **Features**
- **Automatic Java Class Generation**: Generate Java classes directly from Avro schema files.
- **Multiple Schema Formats**: Support for `.avsc` (JSON) and `.avpr` (Protocol) files.
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
- **Gradle Integration**: Fully integrates with both Kotlin DSL and Groovy DSL.
- **Multiple Schema File Support**: Process multiple Avro schema files effortlessly.
- **Automatic Task Creation**: Automatically adds tasks to your Gradle workflow.
- **Simple Configuration**: Intuitive configuration options for quick setup.
---

## **Requirements**
- **Java**: Version 21 or higher.
- **Gradle**: Version 8.0 or higher.
- **Apache Avro**: Version 1.12.0 or higher.

---

**Configuration Options**
Define configuration options in the `avro` block within your project's `build.gradle` or `build.gradle.kts` file. The following options are supported:

| **Option**                | **Description**                                                                     | **Default Value**            | **Allowed Values**           |
|---------------------------|-------------------------------------------------------------------------------------|-----------------------------|-----------------------------|
| `sourceDir`               | Directory containing Avro schema files (`*.avsc`, `*.avpr`).                        | `src/main/resources/avro`   | Custom directory path       |
| `outputDir`               | Directory to save generated Java classes.                                           | `build/generated/java`      | Custom directory path       |
| `fieldVisibility`         | Visibility of generated fields.                                                     | `PUBLIC`                    | `PUBLIC`, `PRIVATE`         |
| `stringType`              | String representation to use in generated classes.                                  | `String`                    | `String`, `CharSequence`, `Utf8` |
| `optionalGetters`         | Enables optional getter methods for generated fields.                               | `false`                     | `true`, `false`             |
| `useDecimalLogical`       | Enables support for decimal logical types in Avro schemas.                          | `false`                     | `true`, `false`             |
| `createNullSafeAnnotations` | Adds null-safe annotations to generated classes.                                   | `false`                     | `true`, `false`             |
| `enableCache`             | Enables Gradle build cache for incremental builds.                                  | `true`                      | `true`, `false`             |
| `validateBeforeGenerate`  | Validates schemas before generation.                                                | `true`                      | `true`, `false`             |
| `generateBuilders`        | Generates builder pattern classes (coming soon).                                    | `false`                     | `true`, `false`             |
| `addCustomHeader`         | Adds custom header to generated files (coming soon).                                | `null`                      | Any string value            |
| `stringType`              | String representation to use in generated classes.                                  | `String`                    | `String`, `CharSequence`, `Utf8` |
| `optionalGetters`         | Enables optional getter methods for generated fields.                               | `false`                     | `true`, `false`             |
| `useDecimalLogical`       | Enables support for decimal logical types in Avro schemas.                          | `false`                     | `true`, `false`             |
| `createNullSafeAnnotations` | Adds null-safe annotations to generated classes.                                   | `false`                     | `true`, `false`             |

---

## **Usage Example**

Add the plugin to your `build.gradle` or `build.gradle.kts` file:

```kotlin
plugins {
    id("io.github.martinsjavacode.avro-gradle-plugin") version "1.0.0"
}

avro {
    group = "build"
    description = "Plugin to generate Avro classes from schema files in the project"
    sourceDir = "src/main/avro"
    outputDir = "build/generated-sources/avro"
    fieldVisibility = "PRIVATE"
    stringType = "CharSequence"
    optionalGetters = true
}
```

Run the task to generate Avro classes:
```bash
  ./gradlew generateAvroClasses
```

Or build the project to include Avro generation:
```bash
  ./gradlew build
```

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

## Authors
<div style="display: flex; width: 100%; border: 1px solid #ddd; border-radius: 10px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); font-family: Arial, sans-serif; background: #333; overflow: hidden;">
  <img src="https://avatars.githubusercontent.com/u/8130295?v=4" alt="Martins Photo" style="height: 200px; object-fit: contain;">
  <div style="padding: 0 20px; text-align: left; flex: 1;">
    <h2 style="font-size: 1.5em; color: #fff;">Alexandre Martins</h2>
    <p style="font-size: 0.9em; color: #fff; margin-bottom: 15px;">Software Engineer with a strong passion for Java and building efficient, scalable solutions. Experienced in various tools and technologies, with a focus on delivering clean, high-quality code and driving innovation.</p>
    <div style="display: inline-block; padding: 6px 10px; background: #007bff; color: #fff; text-decoration: none; border-radius: 5px; transition: background 0.3s ease;">
        amartins.alexandre@hotmail.com
    </div>
  </div>
</div>



---

## **Issues**
If you encounter any issues or have feature requests, please open an issue in the [GitHub repository](https://github.com/martinsjavacode/avro-gradle-plugin/issues).

---

## **License**
This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for full details.
