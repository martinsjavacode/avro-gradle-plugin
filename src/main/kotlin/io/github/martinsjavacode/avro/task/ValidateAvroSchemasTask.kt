package io.github.martinsjavacode.avro.task

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.github.martinsjavacode.avro.validator.SchemaValidator
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.*

abstract class ValidateAvroSchemasTask : DefaultTask() {
	private var extension: AvroPluginExtension = project.extensions.getByType(AvroPluginExtension::class.java)

	@get:InputFiles
	@get:PathSensitive(PathSensitivity.RELATIVE)
	val schemaFiles: FileTree
		get() {
			val sourceDirectory =
				extension.sourceDir?.let { project.file(it) }
					?: project.file("src/main/resources/avro")
			val finalSourceDir = resolveCustomSourceDir(sourceDirectory)
			return project.fileTree(finalSourceDir) {
				include("**/*.avsc", "**/*.avpr")
			}
		}

	@TaskAction
	fun validate() {
		val sourceDirectory =
			extension.sourceDir?.let { project.file(it) }
				?: project.file("src/main/resources/avro")
		val finalSourceDir = resolveCustomSourceDir(sourceDirectory)

		if (!finalSourceDir.exists()) {
			project.logger.lifecycle("Source directory does not exist: $finalSourceDir")
			return
		}

		val validator = SchemaValidator(project)
		val results = validator.validate(finalSourceDir)

		if (results.hasErrors()) {
			project.logger.error("Schema validation failed:")
			results.errors.forEach { project.logger.error("  ✗ $it") }
			throw IllegalStateException("Found ${results.errors.size} validation error(s)")
		}

		project.logger.lifecycle("✓ All schemas validated successfully (${results.validatedCount} files)")
	}

	private fun resolveCustomSourceDir(defaultSourceDir: File): File {
		val propertyFile = project.file("application.properties")
		val yamlFile = project.file("application.yml")

		val customSourceDir =
			when {
				propertyFile.exists() -> {
					Properties().apply { load(propertyFile.inputStream()) }
						.getProperty("sourceDirectory")
				}

				yamlFile.exists() -> {
					Properties().apply { load(yamlFile.inputStream()) }
						.getProperty("sourceDirectory")
				}

				else -> null
			}

		return customSourceDir?.let { project.file(it) } ?: defaultSourceDir
	}
}
