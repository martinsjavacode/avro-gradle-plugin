package io.github.martinsjavacode.avro.task

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class AvroTaskTest :
	StringSpec({
		"should generate classes from valid schema" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val avroDir = File(project.projectDir, "src/main/resources/avro").apply { mkdirs() }
			val buildDir = project.layout.buildDirectory.get().asFile
			val outputDir = File(buildDir, "generated/avro")

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

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.sourceDir.set(avroDir)
			task.outputDir.set(outputDir)
			task.fieldVisibility.set("PUBLIC")
			task.stringType.set("String")
			task.optionalGetters.set(false)
			task.useDecimalLogical.set(false)
			task.createNullSafeAnnotations.set(false)
			task.generateAvroClasses()

			outputDir.shouldExist()
			File(outputDir, "com/example/User.java").shouldExist()

			avroDir.deleteRecursively()
			outputDir.deleteRecursively()
		}

		"should handle empty source directory" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val avroDir = File(project.projectDir, "src/main/resources/avro").apply { mkdirs() }
			val buildDir = project.layout.buildDirectory.get().asFile

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.sourceDir.set(avroDir)
			task.outputDir.set(File(buildDir, "generated/java"))
			task.fieldVisibility.set("PUBLIC")
			task.stringType.set("String")
			task.optionalGetters.set(false)
			task.useDecimalLogical.set(false)
			task.createNullSafeAnnotations.set(false)
			task.generateAvroClasses()

			avroDir.deleteRecursively()
		}

		"should use custom output directory" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

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

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.sourceDir.set(avroDir)
			task.outputDir.set(customOutput)
			task.fieldVisibility.set("PUBLIC")
			task.stringType.set("String")
			task.optionalGetters.set(false)
			task.useDecimalLogical.set(false)
			task.createNullSafeAnnotations.set(false)
			task.generateAvroClasses()

			customOutput.shouldExist()
			File(customOutput, "com/shop/Product.java").shouldExist()

			avroDir.deleteRecursively()
			customOutput.deleteRecursively()
		}

		"should generate from AVPR protocol file" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val avroDir = File(project.projectDir, "avro").apply { mkdirs() }
			val buildDir = project.layout.buildDirectory.get().asFile
			val genDir = File(buildDir, "gen")

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

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.sourceDir.set(avroDir)
			task.outputDir.set(genDir)
			task.fieldVisibility.set("PUBLIC")
			task.stringType.set("String")
			task.optionalGetters.set(false)
			task.useDecimalLogical.set(false)
			task.createNullSafeAnnotations.set(false)
			task.generateAvroClasses()

			File(genDir, "com/test/Message.java").shouldExist()

			avroDir.deleteRecursively()
			genDir.deleteRecursively()
		}

		"should handle subdirectories" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val avroDir = File(project.projectDir, "avro").apply { mkdirs() }
			val subDir = File(avroDir, "models").apply { mkdirs() }
			val buildDir = project.layout.buildDirectory.get().asFile
			val outputDir = File(buildDir, "output")

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

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.sourceDir.set(avroDir)
			task.outputDir.set(outputDir)
			task.fieldVisibility.set("PUBLIC")
			task.stringType.set("String")
			task.optionalGetters.set(false)
			task.useDecimalLogical.set(false)
			task.createNullSafeAnnotations.set(false)
			task.generateAvroClasses()

			File(outputDir, "com/orders/Order.java").shouldExist()

			avroDir.deleteRecursively()
			outputDir.deleteRecursively()
		}

		"should apply field visibility settings" {
			val project = ProjectBuilder.builder().build()
			project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

			val avroDir = File(project.projectDir, "avro").apply { mkdirs() }
			val buildDir = project.layout.buildDirectory.get().asFile
			val outDir = File(buildDir, "out")

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

			val task = project.tasks.getByName("generateAvroClasses") as AvroTask
			task.sourceDir.set(avroDir)
			task.outputDir.set(outDir)
			task.fieldVisibility.set("PRIVATE")
			task.stringType.set("String")
			task.optionalGetters.set(false)
			task.useDecimalLogical.set(false)
			task.createNullSafeAnnotations.set(false)
			task.generateAvroClasses()

			File(outDir, "com/data/Data.java").shouldExist()

			avroDir.deleteRecursively()
			outDir.deleteRecursively()
		}
	})
