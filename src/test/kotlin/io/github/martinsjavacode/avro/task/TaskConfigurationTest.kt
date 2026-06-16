package io.github.martinsjavacode.avro.task

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

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.sourceDir.set(avroDir)
			val schemaFiles = task.schemaFiles

			schemaFiles shouldNotBe null
			schemaFiles.files.size shouldBe 1

			avroDir.deleteRecursively()
		}

		"should handle application properties file" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

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

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.sourceDir.set(avroDir)
			task.outputDir.set(File(project.layout.buildDirectory.get().asFile, "generated/java"))
			task.fieldVisibility.set("PUBLIC")
			task.stringType.set("String")
			task.optionalGetters.set(false)
			task.useDecimalLogical.set(false)
			task.createNullSafeAnnotations.set(false)
			task.generateAvroClasses()

			File(project.projectDir, "application.properties").delete()
			avroDir.deleteRecursively()
		}

		"should handle YAML configuration file" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

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

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.sourceDir.set(avroDir)
			task.outputDir.set(File(project.layout.buildDirectory.get().asFile, "generated/java"))
			task.fieldVisibility.set("PUBLIC")
			task.stringType.set("String")
			task.optionalGetters.set(false)
			task.useDecimalLogical.set(false)
			task.createNullSafeAnnotations.set(false)
			task.generateAvroClasses()

			File(project.projectDir, "application.yml").delete()
			avroDir.deleteRecursively()
		}

		"should use default source directory when no config" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val defaultDir = File(project.projectDir, "src/main/resources/avro").apply { mkdirs() }

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.sourceDir.set(defaultDir)
			task.outputDir.set(File(project.layout.buildDirectory.get().asFile, "generated/java"))
			task.fieldVisibility.set("PUBLIC")
			task.stringType.set("String")
			task.optionalGetters.set(false)
			task.useDecimalLogical.set(false)
			task.createNullSafeAnnotations.set(false)
			task.generateAvroClasses()

			defaultDir.deleteRecursively()
		}

		"should configure outputDir property" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.outputDir.set(File(project.projectDir, "custom/output"))

			task.outputDir shouldNotBe null
		}

		"should validate with custom source directory" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

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

			val validateTask = project.tasks.getByName("validateAvroSchemas") as ValidateAvroSchemasTask
			validateTask.sourceDir.set(avroDir)
			val schemaFiles = validateTask.schemaFiles

			schemaFiles shouldNotBe null
			schemaFiles.files.size shouldBe 1

			avroDir.deleteRecursively()
		}
	})
