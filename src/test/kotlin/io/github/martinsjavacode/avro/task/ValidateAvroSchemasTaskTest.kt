package io.github.martinsjavacode.avro.task

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class ValidateAvroSchemasTaskTest :
	StringSpec({
		"should validate valid schema successfully" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			val avroDir = File(project.projectDir, "src/main/resources/avro").apply { mkdirs() }

			File(avroDir, "user.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "User",
				  "namespace": "com.example",
				  "fields": [
				    {"name": "id", "type": "long"},
				    {"name": "name", "type": "string"}
				  ]
				}
				""".trimIndent(),
			)

			extension.sourceDir = avroDir.absolutePath

			val task = project.tasks.getByName("validateAvroSchemas") as ValidateAvroSchemasTask
			task.validate()

			avroDir.deleteRecursively()
		}

		"should fail on invalid schema" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			val avroDir = File(project.projectDir, "src/main/resources/avro").apply { mkdirs() }

			File(avroDir, "empty.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Empty",
				  "namespace": "com.example",
				  "fields": []
				}
				""".trimIndent(),
			)

			extension.sourceDir = avroDir.absolutePath

			val task = project.tasks.getByName("validateAvroSchemas") as ValidateAvroSchemasTask

			shouldThrow<IllegalStateException> {
				task.validate()
			}

			avroDir.deleteRecursively()
		}

		"should handle non-existent directory" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			extension.sourceDir = "/non/existent/path"

			val task = project.tasks.getByName("validateAvroSchemas") as ValidateAvroSchemasTask
			task.validate()
		}

		"should validate AVPR files" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			val avroDir = File(project.projectDir, "avro").apply { mkdirs() }

			File(avroDir, "protocol.avpr").writeText(
				"""
				{
				  "protocol": "TestProtocol",
				  "namespace": "com.test",
				  "types": [
				    {
				      "type": "record",
				      "name": "Message",
				      "fields": [{"name": "text", "type": "string"}]
				    }
				  ]
				}
				""".trimIndent(),
			)

			extension.sourceDir = avroDir.absolutePath

			val task = project.tasks.getByName("validateAvroSchemas") as ValidateAvroSchemasTask
			task.validate()

			avroDir.deleteRecursively()
		}

		"should validate multiple files" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			val avroDir = File(project.projectDir, "avro").apply { mkdirs() }

			File(avroDir, "user.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "User",
				  "fields": [{"name": "name", "type": "string"}]
				}
				""".trimIndent(),
			)

			File(avroDir, "order.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Order",
				  "fields": [{"name": "id", "type": "long"}]
				}
				""".trimIndent(),
			)

			extension.sourceDir = avroDir.absolutePath

			val task = project.tasks.getByName("validateAvroSchemas") as ValidateAvroSchemasTask
			task.validate()

			avroDir.deleteRecursively()
		}

		"should validate enum schemas" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			val avroDir = File(project.projectDir, "avro").apply { mkdirs() }

			File(avroDir, "status.avsc").writeText(
				"""
				{
				  "type": "enum",
				  "name": "Status",
				  "namespace": "com.example",
				  "symbols": ["ACTIVE", "INACTIVE"]
				}
				""".trimIndent(),
			)

			extension.sourceDir = avroDir.absolutePath

			val task = project.tasks.getByName("validateAvroSchemas") as ValidateAvroSchemasTask
			task.validate()

			avroDir.deleteRecursively()
		}
	})
