package io.github.martinsjavacode.avro.task

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class AvroTaskTest :
	StringSpec({
		"should generate classes from valid schema" {
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
			extension.outputDir = File(project.buildDir, "generated/avro").absolutePath
			extension.validateBeforeGenerate = false

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.generateAvroClasses()

			val outputDir = File(project.buildDir, "generated/avro")
			outputDir.shouldExist()

			val generatedFile = File(outputDir, "com/example/User.java")
			generatedFile.shouldExist()

			avroDir.deleteRecursively()
			outputDir.deleteRecursively()
		}

		"should handle empty source directory" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			val avroDir = File(project.projectDir, "src/main/resources/avro").apply { mkdirs() }

			extension.sourceDir = avroDir.absolutePath
			extension.validateBeforeGenerate = false

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.generateAvroClasses()

			avroDir.deleteRecursively()
		}

		"should use custom output directory" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			val avroDir = File(project.projectDir, "schemas").apply { mkdirs() }
			val customOutput = File(project.projectDir, "custom-output")

			File(avroDir, "product.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Product",
				  "namespace": "com.shop",
				  "fields": [
				    {"name": "id", "type": "string"}
				  ]
				}
				""".trimIndent(),
			)

			extension.sourceDir = avroDir.absolutePath
			extension.outputDir = customOutput.absolutePath
			extension.validateBeforeGenerate = false

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.generateAvroClasses()

			customOutput.shouldExist()
			File(customOutput, "com/shop/Product.java").shouldExist()

			avroDir.deleteRecursively()
			customOutput.deleteRecursively()
		}

		"should generate from AVPR protocol file" {
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
			extension.outputDir = File(project.buildDir, "gen").absolutePath
			extension.validateBeforeGenerate = false

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.generateAvroClasses()

			File(project.buildDir, "gen/com/test/Message.java").shouldExist()

			avroDir.deleteRecursively()
			File(project.buildDir, "gen").deleteRecursively()
		}

		"should handle subdirectories" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			val avroDir = File(project.projectDir, "avro").apply { mkdirs() }
			val subDir = File(avroDir, "models").apply { mkdirs() }

			File(subDir, "order.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Order",
				  "namespace": "com.orders",
				  "fields": [{"name": "id", "type": "long"}]
				}
				""".trimIndent(),
			)

			extension.sourceDir = avroDir.absolutePath
			extension.outputDir = File(project.buildDir, "output").absolutePath
			extension.validateBeforeGenerate = false

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.generateAvroClasses()

			File(project.buildDir, "output/com/orders/Order.java").shouldExist()

			avroDir.deleteRecursively()
			File(project.buildDir, "output").deleteRecursively()
		}

		"should apply field visibility settings" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val extension = project.extensions.getByType(AvroPluginExtension::class.java)
			val avroDir = File(project.projectDir, "avro").apply { mkdirs() }

			File(avroDir, "data.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Data",
				  "namespace": "com.data",
				  "fields": [{"name": "value", "type": "string"}]
				}
				""".trimIndent(),
			)

			extension.sourceDir = avroDir.absolutePath
			extension.outputDir = File(project.buildDir, "out").absolutePath
			extension.fieldVisibility = "PRIVATE"
			extension.validateBeforeGenerate = false

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.generateAvroClasses()

			File(project.buildDir, "out/com/data/Data.java").shouldExist()

			avroDir.deleteRecursively()
			File(project.buildDir, "out").deleteRecursively()
		}
	})
