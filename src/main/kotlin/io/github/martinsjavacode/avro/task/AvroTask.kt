package io.github.martinsjavacode.avro.task

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.github.martinsjavacode.avro.generator.AvroGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.*

abstract class AvroTask : DefaultTask() {
	private var extension: AvroPluginExtension = project.extensions.getByType(AvroPluginExtension::class.java)

	@TaskAction
	fun generateAvroClasses() {
		val sourceDirectory = extension.sourceDir?.let { project.file(it) }
			?: project.file("src/main/resources/avro")
		val outputDirectory = extension.outputDir?.let { project.file(it) }
			?: project.file("build/generated/java")

		outputDirectory.deleteRecursively()
		outputDirectory.mkdirs()

		val finalSourceDir = resolveCustomSourceDir(sourceDirectory)

		if (!finalSourceDir.exists()) {
			project.logger.lifecycle("Source directory does not exist: $finalSourceDir")
			return
		}

		AvroGenerator.process(
			sourceDir = finalSourceDir,
			project = project,
			extension = extension,
			outputDirectory = outputDirectory
		)

		project.logger.lifecycle("Avro classes generated successfully")
	}

	private fun resolveCustomSourceDir(defaultSourceDir: File): File {
		val propertyFile = project.file("application.properties")
		val yamlFile = project.file("application.yml")

		val customSourceDir = when {
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
