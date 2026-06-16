package io.github.martinsjavacode.avro.task

import io.github.martinsjavacode.avro.validator.SchemaValidator
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.*

@CacheableTask
abstract class ValidateAvroSchemasTask : DefaultTask() {
	@get:InputDirectory
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val sourceDir: DirectoryProperty

	@get:InputFiles
	@get:PathSensitive(PathSensitivity.RELATIVE)
	val schemaFiles: FileTree
		get() = sourceDir.asFileTree.matching { include("**/*.avsc", "**/*.avpr") }

	@get:OutputDirectory
	abstract val reportDir: DirectoryProperty

	@TaskAction
	fun validate() {
		val source = sourceDir.get().asFile

		if (!source.exists()) {
			logger.lifecycle("Source directory does not exist: $source")
			return
		}

		val validator = SchemaValidator(logger)
		val results = validator.validate(source)

		if (results.hasErrors()) {
			logger.error("Schema validation failed:")
			results.errors.forEach { logger.error("  ✗ $it") }
			throw IllegalStateException("Found ${results.errors.size} validation error(s)")
		}

		logger.lifecycle("✓ All schemas validated successfully (${results.validatedCount} files)")
	}
}
