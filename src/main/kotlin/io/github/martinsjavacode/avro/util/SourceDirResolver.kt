package io.github.martinsjavacode.avro.util

import org.gradle.api.Project
import java.io.File
import java.util.*

object SourceDirResolver {
	fun resolve(
		project: Project,
		defaultSourceDir: File,
	): File {
		val propertyFile = project.file("application.properties")
		val yamlFile = project.file("application.yml")

		val customSourceDir =
			when {
				propertyFile.exists() -> {
					Properties().apply { load(propertyFile.inputStream()) }
						.getProperty("sourceDirectory")
				}
				yamlFile.exists() -> parseYamlProperty(yamlFile, "sourceDirectory")
				else -> null
			}

		return customSourceDir?.let { project.file(it) } ?: defaultSourceDir
	}

	private fun parseYamlProperty(
		file: File,
		key: String,
	): String? {
		file.useLines { lines ->
			for (line in lines) {
				val trimmed = line.trim()
				if (trimmed.startsWith("$key:")) {
					return trimmed.substringAfter(":").trim().ifBlank { null }
				}
			}
		}
		return null
	}
}
