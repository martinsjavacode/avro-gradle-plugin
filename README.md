# Avro Gradle Plugin

The **Avro Gradle Plugin** simplifies the process of generating Java classes from Avro schema files (`*.avsc`). With a focus on flexibility and ease of use, this plugin integrates seamlessly into the Gradle build process, offering extensive customization options for developers.

---

## **Features**
- **Automatic Java Class Generation**: Generate Java classes directly from Avro schema files.
- **Customizable Directories**: Specify source and output directories for Avro schema files and generated classes.
- **Field Visibility Control**: Configure fields as `public` or `private`.
- **String Type Selection**: Choose between `String`, `CharSequence`, or `Utf8` for string representations.
- **Optional Getters**: Enable or disable optional getter methods in the generated classes.
- **Gradle Integration**: Fully integrates with both Kotlin DSL and Groovy DSL.
- **Multiple Schema File Support**: Process multiple Avro schema files effortlessly.
- **Automatic Task Creation**: Automatically adds a `generateAvroClasses` task to your Gradle workflow.
- **Simple Configuration**: Intuitive configuration options for quick setup.

---

## **Requirements**
- **Java**: Version 21 or higher.
- **Gradle**: Version 8.0 or higher.
- **Apache Avro**: Version 1.12.0 or higher.

---

## **Configuration Options**
Define configuration options in the `avro` block within your project's `build.gradle` or `build.gradle.kts` file. The following options are supported:

| **Option**            | **Description**                                                                     | **Default Value**            | **Allowed Values**           |
|-----------------------|-------------------------------------------------------------------------------------|-----------------------------|-----------------------------|
| `sourceDir`           | Directory containing Avro schema files (`*.avsc`).                                  | `src/main/resources/avro`   | Custom directory path       |
| `outputDir`           | Directory to save generated Java classes.                                           | `build/generated/java`      | Custom directory path       |
| `group`               | Gradle task group.                                                                  | `build`                     | Any string value            |
| `description`         | Description of the Gradle task.                                                     | _Plugin description_        | Any string value            |
| `fieldVisibility`     | Visibility of generated fields.                                                     | `PUBLIC`                    | `PUBLIC`, `PRIVATE`         |
| `stringType`          | String representation to use in generated classes.                                  | `String`                    | `String`, `CharSequence`, `Utf8` |
| `optionalGetters`     | Enables optional getter methods for generated fields.                               | `false`                     | `true`, `false`             |

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

## **Issues**
If you encounter any issues or have feature requests, please open an issue in the [GitHub repository](https://github.com/martinsjavacode/avro-gradle-plugin/issues).

## **License**
This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for full details.
