package io.github.martinsjavacode.avro.generator

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.github.martinsjavacode.avro.report.GeneratedClass
import io.github.martinsjavacode.avro.report.GenerationReport
import org.apache.avro.Protocol
import org.apache.avro.Schema
import org.apache.avro.compiler.specific.SpecificCompiler
import org.apache.avro.compiler.specific.SpecificCompiler.FieldVisibility
import org.apache.avro.generic.GenericData.StringType
import org.gradle.api.Project
import java.io.File

object AvroGenerator {
	fun process(
		sourceDir: File,
		project: Project,
		extension: AvroPluginExtension,
		outputDirectory: File,
	): GenerationReport {
		val errors = mutableListOf<String>()
		val report = GenerationReport()

		sourceDir.walkTopDown()
			.filter { dir ->
				dir.isDirectory && dir.walkTopDown().any { it.isFile }
			}.forEach { subDir ->
				generate(project, extension, subDir, outputDirectory, errors, report)
			}

		if (errors.isNotEmpty()) {
			project.logger.error("Errors found during generation:")
			errors.forEach { project.logger.error("  - $it") }
			throw IllegalStateException("Schema generation failed with ${errors.size} error(s)")
		}

		// Gerar relatório HTML
		// TODO: Melhorar tratamento de erro para geração de relatório em ambientes de teste
		try {
			val reportDir = File(project.layout.buildDirectory.asFile.get(), "reports/avro")
			reportDir.mkdirs()
			report.generateHtmlReport(reportDir)
			project.logger.lifecycle("Report generated: ${reportDir.absolutePath}/avro-generation-report.html")
		} catch (e: Exception) {
			project.logger.warn("Could not generate HTML report: ${e.message}")
		}

		return report
	}

	private fun generate(
		project: Project,
		extension: AvroPluginExtension,
		sourceDirectory: File,
		outputDirectory: File,
		errors: MutableList<String>,
		report: GenerationReport,
	) {
		sourceDirectory.listFiles { file ->
			file.extension in listOf("avsc", "avpr")
		}?.forEach { file ->
			try {
				when (file.extension) {
					"avsc" -> processAvsc(file, extension, outputDirectory, project, report)
					"avpr" -> processAvpr(file, extension, outputDirectory, project, report)
				}
			} catch (e: Exception) {
				val errorMsg = "Error processing ${file.name}: ${e.message}"
				errors.add(errorMsg)
				project.logger.error(errorMsg, e)
			}
		}
	}

	private fun processAvsc(
		file: File,
		extension: AvroPluginExtension,
		outputDirectory: File,
		project: Project,
		report: GenerationReport,
	) {
		val schema = Schema.Parser().parse(file)
		validateSchema(schema)
		val outputFile = compileSchema(schema, file, extension, outputDirectory)
		report.addClass(
			GeneratedClass(
				name = schema.name,
				sourceFile = file.name,
				outputFile = outputFile.relativeTo(outputDirectory).path,
				type = "AVSC",
			),
		)
		project.logger.lifecycle("Generated class from AVSC: ${schema.name}")
	}

	private fun processAvpr(
		file: File,
		extension: AvroPluginExtension,
		outputDirectory: File,
		project: Project,
		report: GenerationReport,
	) {
		val protocol = Protocol.parse(file)
		protocol.types.forEach { schema ->
			validateSchema(schema)
			val outputFile = compileSchema(schema, file, extension, outputDirectory)
			report.addClass(
				GeneratedClass(
					name = schema.name,
					sourceFile = file.name,
					outputFile = outputFile.relativeTo(outputDirectory).path,
					type = "AVPR",
				),
			)
			project.logger.lifecycle("Generated class from AVPR: ${schema.name}")
		}
	}

	private fun validateSchema(schema: Schema) {
		when (schema.type) {
			Schema.Type.RECORD -> {
				require(schema.fields.isNotEmpty()) { "Record ${schema.name} has no fields" }
				schema.fields.forEach { field ->
					require(field.name().isNotBlank()) { "Field name cannot be blank in ${schema.name}" }
				}
			}
			Schema.Type.ENUM -> {
				require(schema.enumSymbols.isNotEmpty()) { "Enum ${schema.name} has no symbols" }
			}
			else -> {}
		}
	}

	private fun compileSchema(
		schema: Schema,
		sourceFile: File,
		extension: AvroPluginExtension,
		outputDirectory: File,
	): File {
		val compiler =
			SpecificCompiler(schema).apply {
				isCreateOptionalGetters = extension.optionalGetters
				isCreateSetters = extension.fieldVisibility == "PUBLIC"
				isCreateNullSafeAnnotations = extension.createNullSafeAnnotations
				setFieldVisibility(FieldVisibility.valueOf(extension.fieldVisibility))
				setStringType(StringType.valueOf(extension.stringType))
				setEnableDecimalLogicalType(extension.useDecimalLogical)
			}

		compiler.compileToDestination(sourceFile, outputDirectory)

		// Retorna o arquivo gerado
		val packagePath = schema.namespace?.replace('.', '/') ?: ""
		return File(outputDirectory, "$packagePath/${schema.name}.java")
	}
}
