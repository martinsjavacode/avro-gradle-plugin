package io.github.martinsjavacode.avro.generator

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.kotest.core.spec.style.FunSpec
import io.mockk.*
import org.apache.avro.Schema
import org.apache.avro.compiler.specific.SpecificCompiler
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

class AvroGeneratorTest : FunSpec({
	lateinit var extension: AvroPluginExtension
	lateinit var project: Project
	lateinit var sourceDirectory: File
	lateinit var outputDirectory: File

	beforeTest {
		extension = mockk(relaxed = true) {
			every { fieldVisibility } returns "PUBLIC"
			every { stringType } returns "String"
		}
		project = mockk(relaxed = true)

		sourceDirectory = createTempDirectory("sourceDir").toFile()
		outputDirectory = createTempDirectory("outputDir").toFile()

		// Create the source directory and add a sample schema file
		val resourceDir = Path.of("src/test/resources/avro")
		Files.walk(resourceDir).forEach { path ->
			if (path.toFile().isFile && path.toString().endsWith(".avsc")) {
				path.toFile()
					.copyTo(
						sourceDirectory.resolve(path.fileName.toString()),
						overwrite = true
					)
			}
		}

		File(sourceDirectory, "sourceSubDirectory").apply {
			mkdirs()
			resolve("schema1.avsc").writeText(
				"""
				{
					"type": "record",
					"connect.name": "io.github.amartins.sample.User",
					"namespace": "io.github.amartins",
					"name": "User",
					"fields": [
						{"name": "id", "type": "string"},
						{"name": "name", "type": "string"},
						{"name": "email", "type": "string"},
						{"name": "age", "type": ["null", "int"], "default": null}
					]
				}
			""".trimIndent()
			)
		}
	}

	afterTest {
		sourceDirectory.deleteRecursively()
		outputDirectory.deleteRecursively()
		unmockkAll()
	}

	test("should process valid schema files and generate classes") {
		mockkConstructor(Schema.Parser::class)
		mockkConstructor(SpecificCompiler::class)

		every { anyConstructed<Schema.Parser>().parse(any<File>()) } returns mockk(relaxed = true)
		every { anyConstructed<SpecificCompiler>().compileToDestination(any(), any()) } just Runs

		AvroGenerator.process(
			sourceDir = sourceDirectory,
			project = project,
			extension = extension,
			outputDirectory = outputDirectory
		)

		verify(exactly = 2) { anyConstructed<Schema.Parser>().parse(any<File>()) }
		verify(exactly = 2) { anyConstructed<SpecificCompiler>().compileToDestination(any(), outputDirectory) }
	}

	test("should handle invalid schema files gracefully") {
		File(sourceDirectory, "schema1.avsc").delete()
		val invalidFile = File(sourceDirectory, "invalidSchema.avsc").apply {
			writeText("{ invalid-json }")
		}

		mockkConstructor(Schema.Parser::class)
		every { anyConstructed<Schema.Parser>().parse(invalidFile) } throws IOException("Invalid schema")

		AvroGenerator.process(
			sourceDir = sourceDirectory,
			project = project,
			extension = extension,
			outputDirectory = outputDirectory
		)

		verify { project.logger.error("Error processing file: ${invalidFile.name}", any()) }
	}

	test("should handle empty source directory") {
		sourceDirectory.deleteRecursively()

		mockkConstructor(Schema.Parser::class)
		mockkConstructor(SpecificCompiler::class)

		AvroGenerator.process(
			sourceDir = sourceDirectory,
			project = project,
			extension = extension,
			outputDirectory = outputDirectory
		)

		verify(exactly = 0) { anyConstructed<Schema.Parser>().parse(any<File>()) }
		verify(exactly = 0) { anyConstructed<SpecificCompiler>().compileToDestination(any(), outputDirectory) }
	}
})
