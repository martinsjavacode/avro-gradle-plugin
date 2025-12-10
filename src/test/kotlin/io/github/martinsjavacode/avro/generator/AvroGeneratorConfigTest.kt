package io.github.martinsjavacode.avro.generator

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.apache.avro.Schema
import org.apache.avro.compiler.specific.SpecificCompiler
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File
import kotlin.io.path.createTempDirectory

class AvroGeneratorConfigTest :
	StringSpec({
		"should apply all extension configurations" {
			val extension =
				mockk<AvroPluginExtension>(relaxed = true) {
					every { fieldVisibility } returns "PRIVATE"
					every { stringType } returns "CharSequence"
					every { optionalGetters } returns true
					every { createNullSafeAnnotations } returns true
					every { useDecimalLogical } returns true
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

			File(sourceDirectory, "config.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Config",
				  "namespace": "com.config",
				  "fields": [{"name": "value", "type": "string"}]
				}
				""".trimIndent(),
			)

			mockkConstructor(Schema.Parser::class)
			mockkConstructor(SpecificCompiler::class)

			val mockSchema =
				mockk<Schema>(relaxed = true) {
					every { name } returns "Config"
					every { namespace } returns "com.config"
					every { type } returns Schema.Type.RECORD
					every { fields } returns
						listOf(
							mockk(relaxed = true) {
								every { name() } returns "value"
							},
						)
				}

			every { anyConstructed<Schema.Parser>().parse(any<File>()) } returns mockSchema
			every { anyConstructed<SpecificCompiler>().compileToDestination(any(), any()) } just Runs

			val report =
				AvroGenerator.process(
					sourceDir = sourceDirectory,
					project = project,
					extension = extension,
					outputDirectory = outputDirectory,
				)

			report.getClassCount() shouldBe 1

			// Verificar se as configurações foram aplicadas
			verify { anyConstructed<SpecificCompiler>().isCreateOptionalGetters = true }
			verify { anyConstructed<SpecificCompiler>().isCreateSetters = false }
			verify { anyConstructed<SpecificCompiler>().isCreateNullSafeAnnotations = true }

			sourceDirectory.deleteRecursively()
			outputDirectory.deleteRecursively()
			buildDir.deleteRecursively()
		}

		"should handle report generation failure gracefully" {
			val extension =
				mockk<AvroPluginExtension>(relaxed = true) {
					every { fieldVisibility } returns "PUBLIC"
					every { stringType } returns "String"
					every { optionalGetters } returns false
					every { createNullSafeAnnotations } returns false
					every { useDecimalLogical } returns false
				}

			val logger = mockk<Logger>(relaxed = true)
			val project =
				mockk<Project>(relaxed = true) {
					every { this@mockk.logger } returns logger
					every { layout.buildDirectory.asFile.get() } throws RuntimeException("Build dir not available")
				}

			val sourceDirectory = createTempDirectory("sourceDir").toFile()
			val outputDirectory = createTempDirectory("outputDir").toFile()

			File(sourceDirectory, "simple.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Simple",
				  "namespace": "com.simple",
				  "fields": [{"name": "data", "type": "string"}]
				}
				""".trimIndent(),
			)

			mockkConstructor(Schema.Parser::class)
			mockkConstructor(SpecificCompiler::class)

			val mockSchema =
				mockk<Schema>(relaxed = true) {
					every { name } returns "Simple"
					every { namespace } returns "com.simple"
					every { type } returns Schema.Type.RECORD
					every { fields } returns
						listOf(
							mockk(relaxed = true) {
								every { name() } returns "data"
							},
						)
				}

			every { anyConstructed<Schema.Parser>().parse(any<File>()) } returns mockSchema
			every { anyConstructed<SpecificCompiler>().compileToDestination(any(), any()) } just Runs

			val report =
				AvroGenerator.process(
					sourceDir = sourceDirectory,
					project = project,
					extension = extension,
					outputDirectory = outputDirectory,
				)

			report.getClassCount() shouldBe 1

			// Verificar que o warning foi logado
			verify { logger.warn(match { it.contains("Could not generate HTML report") }) }

			sourceDirectory.deleteRecursively()
			outputDirectory.deleteRecursively()
		}

		"should handle different schema types in same directory" {
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

			// Criar diferentes tipos de schema
			File(sourceDirectory, "record.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "TestRecord",
				  "namespace": "com.test",
				  "fields": [{"name": "id", "type": "string"}]
				}
				""".trimIndent(),
			)

			File(sourceDirectory, "enum.avsc").writeText(
				"""
				{
				  "type": "enum",
				  "name": "TestEnum",
				  "namespace": "com.test",
				  "symbols": ["A", "B", "C"]
				}
				""".trimIndent(),
			)

			mockkConstructor(Schema.Parser::class)
			mockkConstructor(SpecificCompiler::class)

			val mockRecordSchema =
				mockk<Schema>(relaxed = true) {
					every { name } returns "TestRecord"
					every { namespace } returns "com.test"
					every { type } returns Schema.Type.RECORD
					every { fields } returns
						listOf(
							mockk(relaxed = true) {
								every { name() } returns "id"
							},
						)
				}

			val mockEnumSchema =
				mockk<Schema>(relaxed = true) {
					every { name } returns "TestEnum"
					every { namespace } returns "com.test"
					every { type } returns Schema.Type.ENUM
					every { enumSymbols } returns listOf("A", "B", "C")
				}

			every { anyConstructed<Schema.Parser>().parse(any<File>()) } returnsMany listOf(mockRecordSchema, mockEnumSchema)
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
	})
