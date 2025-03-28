# Avro Gradle Plugin

This project is a Gradle plugin for generating Java classes from Avro schema files (\*.avsc). It allows configuring source and output directories, field visibility, string type, and whether to create optional getters.

## Configuration Options

Configuration options can be set in your project's `build.gradle.kts` file in the `avro` block:

- `sourceDir`: Directory where the Avro schema files (\*.avsc) are located.
  - Default: `src/main/resources/avro`.
- `outputDir`: Directory where the generated Java classes will be saved.
  - Default: `build/generated/java`.
- `group`: Gradle task group.
   - Default: `build`.
- `description`: Gradle task description.
  - Default: `Plugin to generate Avro classes from schema files in the project`.
- `fieldVisibility`: Visibility of the generated fields.
  - Default: `PUBLIC`.
  - Options: `PUBLIC` or `PRIVATE`.
- `stringType`: String type to be used in the generated classes.
  - Default: `String`.
  - Options: `String`, `CharSequence`, or `Utf8`.
- `optionalGetters`: Whether to create optional getters.
  - Default: `false`.

## Usage Example

Add the plugin to your `build.gradle.kts` file:

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

Run the task to generate the classes:

```bash
./gradlew generateAvroClasses
```

## Contributing

- Fork the repository.
- Create a branch for your feature (git checkout -b feature/new-feature).
- Commit your changes (git commit -am 'Add new feature').
- Push to the branch (git push origin feature/new-feature).
- Open a Pull Request.

## Issues
This project is actively maintained. If you encounter any issues, please open an issue on the GitHub repository.

## License
This project is licensed under the Apache License. See the [LICENSE](LICENSE) file for more details.
