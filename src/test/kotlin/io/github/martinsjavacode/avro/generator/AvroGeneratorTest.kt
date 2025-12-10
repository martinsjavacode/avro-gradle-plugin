package io.github.martinsjavacode.avro.generator

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.apache.avro.Schema
import org.apache.avro.compiler.specific.SpecificCompiler
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File
import java.io.IOException
import kotlin.io.path.createTempDirectory

class AvroGeneratorTest :
	FunSpec({
		lateinit var extension: AvroPluginExtension
		lateinit var project: Project
		lateinit var sourceDirectory: File
		lateinit var outputDirectory: File
		lateinit var buildDir: File

		beforeTest {
			extension =
				mockk(relaxed = true) {
					every { fieldVisibility } returns "PUBLIC"
					every { stringType } returns "String"
					every { optionalGetters } returns false
					every { createNullSafeAnnotations } returns false
					every { useDecimalLogical } returns false
				}

			buildDir = createTempDirectory("build").toFile()
			val logger = mockk<Logger>(relaxed = true)

			project =
				mockk(relaxed = true) {
					every { this@mockk.logger } returns logger
					every { layout.buildDirectory.asFile.get() } returns buildDir
				}

			sourceDirectory = createTempDirectory("sourceDir").toFile()
			outputDirectory = createTempDirectory("outputDir").toFile()

			File(sourceDirectory, "sourceSubDirectory").apply {
				mkdirs()
				resolve("user.avsc").writeText(
					"""
					{
						"type": "record",
						"namespace": "io.github.amartins",
						"name": "User",
						"fields": [
							{"name": "id", "type": "string"},
							{"name": "name", "type": "string"}
						]
					}
					""".trimIndent(),
				)
			}
		}

		afterTest {
			sourceDirectory.deleteRecursively()
			outputDirectory.deleteRecursively()
			buildDir.deleteRecursively()
			unmockkAll()
		}

		test("should process valid schema files and generate classes") {
			mockkConstructor(Schema.Parser::class)
			mockkConstructor(SpecificCompiler::class)

			val mockSchema =
				mockk<Schema>(relaxed = true) {
					every { name } returns "User"
					every { namespace } returns "io.github.amartins"
					every { type } returns Schema.Type.RECORD
					every { fields } returns
						listOf(
							mockk(relaxed = true) {
								every { name() } returns "id"
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
		}

		test("should handle invalid schema files gracefully") {
			File(sourceDirectory, "sourceSubDirectory/user.avsc").delete()
			File(sourceDirectory, "invalid.avsc").writeText("{ invalid-json }")

			mockkConstructor(Schema.Parser::class)
			every { anyConstructed<Schema.Parser>().parse(any<File>()) } throws IOException("Invalid schema")

			var exceptionThrown = false
			try {
				AvroGenerator.process(
					sourceDir = sourceDirectory,
					project = project,
					extension = extension,
					outputDirectory = outputDirectory,
				)
			} catch (e: IllegalStateException) {
				exceptionThrown = true
			}

			exceptionThrown shouldBe true
		}

		test("should handle empty source directory") {
			sourceDirectory.deleteRecursively()
			sourceDirectory.mkdirs()

			val report =
				AvroGenerator.process(
					sourceDir = sourceDirectory,
					project = project,
					extension = extension,
					outputDirectory = outputDirectory,
				)

			report.getClassCount() shouldBe 0
		}
	})
