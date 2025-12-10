package io.github.martinsjavacode.avro.validator

import org.apache.avro.Protocol
import org.apache.avro.Schema
import org.gradle.api.Project
import java.io.File

class SchemaValidator(private val project: Project) {
	fun validate(sourceDir: File): ValidationResult {
		val errors = mutableListOf<String>()
		var validatedCount = 0

		sourceDir.walkTopDown()
			.filter { it.isFile && it.extension in listOf("avsc", "avpr") }
			.forEach { file ->
				try {
					when (file.extension) {
						"avsc" -> validateAvsc(file)
						"avpr" -> validateAvpr(file)
					}
					validatedCount++
					project.logger.lifecycle("✓ ${file.name}")
				} catch (e: Exception) {
					errors.add("${file.name}: ${e.message}")
				}
			}

		return ValidationResult(errors, validatedCount)
	}

	private fun validateAvsc(file: File) {
		val schema = Schema.Parser().parse(file)
		validateSchemaStructure(schema, file.name)
	}

	private fun validateAvpr(file: File) {
		val protocol = Protocol.parse(file)
		protocol.types.forEach { schema ->
			validateSchemaStructure(schema, file.name)
		}
	}

	private fun validateSchemaStructure(
		schema: Schema,
		fileName: String,
	) {
		when (schema.type) {
			Schema.Type.RECORD -> {
				require(schema.fields.isNotEmpty()) {
					"Record ${schema.name} in $fileName has no fields"
				}
				schema.fields.forEach { field ->
					require(field.name().isNotBlank()) {
						"Field name cannot be blank in ${schema.name} ($fileName)"
					}
					validateFieldType(field.schema(), schema.name, fileName)
				}
			}
			Schema.Type.ENUM -> {
				require(schema.enumSymbols.isNotEmpty()) {
					"Enum ${schema.name} in $fileName has no symbols"
				}
			}
			Schema.Type.ARRAY -> {
				validateFieldType(schema.elementType, schema.name ?: "array", fileName)
			}
			Schema.Type.MAP -> {
				validateFieldType(schema.valueType, schema.name ?: "map", fileName)
			}
			Schema.Type.UNION -> {
				require(schema.types.isNotEmpty()) {
					"Union in $fileName has no types"
				}
			}
			else -> {}
		}
	}

	private fun validateFieldType(
		fieldSchema: Schema,
		parentName: String,
		fileName: String,
	) {
		when (fieldSchema.type) {
			Schema.Type.RECORD -> validateSchemaStructure(fieldSchema, fileName)
			Schema.Type.ARRAY -> validateFieldType(fieldSchema.elementType, parentName, fileName)
			Schema.Type.MAP -> validateFieldType(fieldSchema.valueType, parentName, fileName)
			Schema.Type.UNION -> {
				fieldSchema.types.forEach { validateFieldType(it, parentName, fileName) }
			}
			else -> {}
		}
	}
}

data class ValidationResult(
	val errors: List<String>,
	val validatedCount: Int,
) {
	fun hasErrors(): Boolean = errors.isNotEmpty()
}
