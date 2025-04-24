package io.github.martinsjavacode.avro.generator

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldExist
import io.mockk.*
import org.apache.avro.Schema
import org.apache.avro.compiler.specific.SpecificCompiler
import org.gradle.api.Project
import java.io.File
import kotlin.io.path.createTempDirectory

class AvroGeneratorTest : FunSpec({
	lateinit var extension: AvroPluginExtension
	lateinit var project: Project
	lateinit var sourceDirectory: File
	lateinit var outputDirectory: File

	beforeTest {
		extension = mockk<AvroPluginExtension>(relaxed = true)
			.apply {
				every { fieldVisibility } returns "PUBLIC"
				every { stringType } returns "String"
			}
		project = mockk<Project>(relaxed = true)

		// Create temporary directories for source and output
		sourceDirectory = createTempDirectory("sourceDir").toFile()
		outputDirectory = createTempDirectory("outputDir").toFile()

		// Create sample schema files in the source directory
		File(sourceDirectory, "schema1.avsc").writeText(
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

		File(sourceDirectory, "schema2.avsc").writeText(
			"""
            {
              	"type": "record",
                "connect.name": "io.github.amartins.sample.User",
                "namespace": "io.github.amartins",
				"name": "TestRecord2",
				"fields": [
					{"name": "field2", "type": "int"}
				]
            }
        """.trimIndent()
		)
	}

	afterTest {
		// Clean up temporary directories
		sourceDirectory.deleteRecursively()
		outputDirectory.deleteRecursively()
	}

	test("should generate Avro classes for valid schema files") {
		mockkConstructor(Schema.Parser::class).apply {
			every { anyConstructed<Schema.Parser>().parse(any<File>()) } returns mockk<Schema>(relaxed =  true)
		}

		mockkConstructor(SpecificCompiler::class).apply {
			every { anyConstructed<SpecificCompiler>().compileToDestination(any(), any()) } just Runs
		}


		// When
		AvroGenerator.generate(project, extension, sourceDirectory, outputDirectory)

		// Then
		verify(exactly = 2) { anyConstructed<Schema.Parser>().parse(any<File>()) }
		verify(exactly = 2) { anyConstructed<SpecificCompiler>().compileToDestination(any(), outputDirectory) }

		// Check if output directory exists
		outputDirectory.shouldExist()
	}

	test("should log a message if no schema files are found") {
		// Given
		sourceDirectory.deleteRecursively() // Remove all files from the source directory

		// When
		AvroGenerator.generate(project, extension, sourceDirectory, outputDirectory)

		// Then
		verify { project.logger.lifecycle("No Avro schema files found in $sourceDirectory") }
	}
})
