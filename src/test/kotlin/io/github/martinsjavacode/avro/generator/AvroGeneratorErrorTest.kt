package io.github.martinsjavacode.avro.generator

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.apache.avro.Schema
import org.apache.avro.compiler.specific.SpecificCompiler
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File
import kotlin.io.path.createTempDirectory

class AvroGeneratorErrorTest :
	StringSpec({
		"should handle AVPR with multiple types" {
			val extension =
				mockk<AvroPluginExtension>(relaxed = true) {
					every { fieldVisibility } returns "PUBLIC"
					every { stringType } returns "String"
					every { optionalGetters } returns false
					every { createNullSafeAnnotations } returns false
					every { useDecimalLogical } returns false
				}

			val buildDir = createTempDirectory("build").toFile()
			val logger = mockk<Logger>(relaxed = true)
			val project =
				mockk<Project>(relaxed = true) {
					every { this@mockk.logger } returns logger
					every { layout.buildDirectory.asFile.get() } returns buildDir
				}

			val sourceDirectory = createTempDirectory("sourceDir").toFile()
			val outputDirectory = createTempDirectory("outputDir").toFile()

			File(sourceDirectory, "multi.avpr").writeText(
				"""
				{
				  "protocol": "MultiProtocol",
				  "namespace": "com.multi",
				  "types": [
				    {
				      "type": "record",
				      "name": "User",
				      "fields": [{"name": "name", "type": "string"}]
				    },
				    {
				      "type": "record",
				      "name": "Order",
				      "fields": [{"name": "id", "type": "long"}]
				    }
				  ]
				}
				""".trimIndent(),
			)

			mockkConstructor(SpecificCompiler::class)
			every { anyConstructed<SpecificCompiler>().compileToDestination(any(), any()) } just Runs

			val report =
				AvroGenerator.process(
					sourceDir = sourceDirectory,
					project = project,
					extension = extension,
					outputDirectory = outputDirectory,
				)

			report.getClassCount() shouldBe 2

			sourceDirectory.deleteRecursively()
			outputDirectory.deleteRecursively()
			buildDir.deleteRecursively()
		}

		"should validate enum with symbols" {
			val extension =
				mockk<AvroPluginExtension>(relaxed = true) {
					every { fieldVisibility } returns "PUBLIC"
					every { stringType } returns "String"
					every { optionalGetters } returns false
					every { createNullSafeAnnotations } returns false
					every { useDecimalLogical } returns false
				}

			val buildDir = createTempDirectory("build").toFile()
			val logger = mockk<Logger>(relaxed = true)
			val project =
				mockk<Project>(relaxed = true) {
					every { this@mockk.logger } returns logger
					every { layout.buildDirectory.asFile.get() } returns buildDir
				}

			val sourceDirectory = createTempDirectory("sourceDir").toFile()
			val outputDirectory = createTempDirectory("outputDir").toFile()

			File(sourceDirectory, "status.avsc").writeText(
				"""
				{
				  "type": "enum",
				  "name": "Status",
				  "namespace": "com.example",
				  "symbols": ["ACTIVE", "INACTIVE"]
				}
				""".trimIndent(),
			)

			mockkConstructor(SpecificCompiler::class)
			every { anyConstructed<SpecificCompiler>().compileToDestination(any(), any()) } just Runs

			val report =
				AvroGenerator.process(
					sourceDir = sourceDirectory,
					project = project,
					extension = extension,
					outputDirectory = outputDirectory,
				)

			report.getClassCount() shouldBe 1

			sourceDirectory.deleteRecursively()
			outputDirectory.deleteRecursively()
			buildDir.deleteRecursively()
		}

		"should fail on enum without symbols" {
			val extension =
				mockk<AvroPluginExtension>(relaxed = true) {
					every { fieldVisibility } returns "PUBLIC"
					every { stringType } returns "String"
					every { optionalGetters } returns false
					every { createNullSafeAnnotations } returns false
					every { useDecimalLogical } returns false
				}

			val buildDir = createTempDirectory("build").toFile()
			val logger = mockk<Logger>(relaxed = true)
			val project =
				mockk<Project>(relaxed = true) {
					every { this@mockk.logger } returns logger
					every { layout.buildDirectory.asFile.get() } returns buildDir
				}

			val sourceDirectory = createTempDirectory("sourceDir").toFile()
			val outputDirectory = createTempDirectory("outputDir").toFile()

			File(sourceDirectory, "empty-enum.avsc").writeText(
				"""
				{
				  "type": "enum",
				  "name": "EmptyEnum",
				  "namespace": "com.example",
				  "symbols": []
				}
				""".trimIndent(),
			)

			mockkConstructor(Schema.Parser::class)
			val mockSchema =
				mockk<Schema>(relaxed = true) {
					every { name } returns "EmptyEnum"
					every { namespace } returns "com.example"
					every { type } returns Schema.Type.ENUM
					every { enumSymbols } returns emptyList()
				}
			every { anyConstructed<Schema.Parser>().parse(any<File>()) } returns mockSchema

			shouldThrow<IllegalStateException> {
				AvroGenerator.process(
					sourceDir = sourceDirectory,
					project = project,
					extension = extension,
					outputDirectory = outputDirectory,
				)
			}

			sourceDirectory.deleteRecursively()
			outputDirectory.deleteRecursively()
			buildDir.deleteRecursively()
		}

		"should fail on record without fields" {
			val extension =
				mockk<AvroPluginExtension>(relaxed = true) {
					every { fieldVisibility } returns "PUBLIC"
					every { stringType } returns "String"
					every { optionalGetters } returns false
					every { createNullSafeAnnotations } returns false
					every { useDecimalLogical } returns false
				}

			val buildDir = createTempDirectory("build").toFile()
			val logger = mockk<Logger>(relaxed = true)
			val project =
				mockk<Project>(relaxed = true) {
					every { this@mockk.logger } returns logger
					every { layout.buildDirectory.asFile.get() } returns buildDir
				}

			val sourceDirectory = createTempDirectory("sourceDir").toFile()
			val outputDirectory = createTempDirectory("outputDir").toFile()

			File(sourceDirectory, "empty-record.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "EmptyRecord",
				  "namespace": "com.example",
				  "fields": []
				}
				""".trimIndent(),
			)

			mockkConstructor(Schema.Parser::class)
			val mockSchema =
				mockk<Schema>(relaxed = true) {
					every { name } returns "EmptyRecord"
					every { namespace } returns "com.example"
					every { type } returns Schema.Type.RECORD
					every { fields } returns emptyList()
				}
			every { anyConstructed<Schema.Parser>().parse(any<File>()) } returns mockSchema

			shouldThrow<IllegalStateException> {
				AvroGenerator.process(
					sourceDir = sourceDirectory,
					project = project,
					extension = extension,
					outputDirectory = outputDirectory,
				)
			}

			sourceDirectory.deleteRecursively()
			outputDirectory.deleteRecursively()
			buildDir.deleteRecursively()
		}
	})
