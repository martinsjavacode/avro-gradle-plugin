package io.github.martinsjavacode.avro.task

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class TaskConfigurationTest :
	StringSpec({
		"should configure schemaFiles property correctly" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			val avroDir = File(project.projectDir, "custom/avro").apply { mkdirs() }

			File(avroDir, "test.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Test",
				  "fields": [{"name": "id", "type": "string"}]
				}
				""".trimIndent(),
			)

			extension.sourceDir = avroDir.absolutePath

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			val schemaFiles = task.schemaFiles

			schemaFiles shouldNotBe null
			schemaFiles.files.size shouldBe 1

			avroDir.deleteRecursively()
		}

		"should handle application properties file" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			val avroDir = File(project.projectDir, "props-avro").apply { mkdirs() }

			File(project.projectDir, "application.properties").writeText(
				"sourceDirectory=${avroDir.absolutePath}",
			)

			File(avroDir, "props.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Props",
				  "fields": [{"name": "value", "type": "string"}]
				}
				""".trimIndent(),
			)

			extension.validateBeforeGenerate = false

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.generateAvroClasses()

			File(project.projectDir, "application.properties").delete()
			avroDir.deleteRecursively()
		}

		"should handle YAML configuration file" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			val avroDir = File(project.projectDir, "yaml-avro").apply { mkdirs() }

			File(project.projectDir, "application.yml").writeText(
				"sourceDirectory: ${avroDir.absolutePath}",
			)

			File(avroDir, "yaml.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Yaml",
				  "fields": [{"name": "config", "type": "string"}]
				}
				""".trimIndent(),
			)

			extension.validateBeforeGenerate = false

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.generateAvroClasses()

			File(project.projectDir, "application.yml").delete()
			avroDir.deleteRecursively()
		}

		"should use default source directory when no config" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			extension.validateBeforeGenerate = false

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.generateAvroClasses()
		}

		"should configure outputDir property" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			extension.outputDir = "custom/output"

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			val outputDir = task.outputDir

			outputDir shouldNotBe null
		}

		"should validate with custom source directory" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			val avroDir = File(project.projectDir, "validate-dir").apply { mkdirs() }

			File(avroDir, "validate.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Validate",
				  "fields": [{"name": "data", "type": "string"}]
				}
				""".trimIndent(),
			)

			extension.sourceDir = avroDir.absolutePath

			val validateTask = project.tasks.getByName("validateAvroSchemas") as ValidateAvroSchemasTask
			val schemaFiles = validateTask.schemaFiles

			schemaFiles shouldNotBe null
			schemaFiles.files.size shouldBe 1

			avroDir.deleteRecursively()
		}
	})
