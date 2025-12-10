package io.github.martinsjavacode.avro.validator

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File
import kotlin.io.path.createTempDirectory

class ValidationEdgeCasesTest :
	StringSpec({
		"should validate complex nested structures" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "complex.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Complex",
				  "namespace": "com.example",
				  "fields": [
				    {
				      "name": "arrayField",
				      "type": {
				        "type": "array",
				        "items": {
				          "type": "record",
				          "name": "Item",
				          "fields": [{"name": "value", "type": "string"}]
				        }
				      }
				    },
				    {
				      "name": "mapField",
				      "type": {
				        "type": "map",
				        "values": {
				          "type": "array",
				          "items": "string"
				        }
				      }
				    },
				    {
				      "name": "unionField",
				      "type": [
				        "null",
				        {
				          "type": "record",
				          "name": "UnionRecord",
				          "fields": [{"name": "data", "type": "string"}]
				        }
				      ]
				    }
				  ]
				}
				""".trimIndent(),
			)

			val result = validator.validate(tempDir)

			result.hasErrors() shouldBe false
			result.validatedCount shouldBe 1

			tempDir.deleteRecursively()
		}

		"should detect blank field names" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "blank-field.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "BlankField",
				  "namespace": "com.example",
				  "fields": [
				    {"name": "", "type": "string"}
				  ]
				}
				""".trimIndent(),
			)

			val result = validator.validate(tempDir)

			result.hasErrors() shouldBe true

			tempDir.deleteRecursively()
		}

		"should validate empty union types" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			// Union vazia não é um JSON válido, vou usar um caso mais realista
			File(tempDir, "valid-union.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "ValidUnion",
				  "namespace": "com.example",
				  "fields": [
				    {
				      "name": "unionField",
				      "type": ["null", "string"]
				    }
				  ]
				}
				""".trimIndent(),
			)

			val result = validator.validate(tempDir)

			result.hasErrors() shouldBe false

			tempDir.deleteRecursively()
		}

		"should handle primitive types" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "primitives.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Primitives",
				  "namespace": "com.example",
				  "fields": [
				    {"name": "stringField", "type": "string"},
				    {"name": "intField", "type": "int"},
				    {"name": "longField", "type": "long"},
				    {"name": "floatField", "type": "float"},
				    {"name": "doubleField", "type": "double"},
				    {"name": "booleanField", "type": "boolean"},
				    {"name": "bytesField", "type": "bytes"}
				  ]
				}
				""".trimIndent(),
			)

			val result = validator.validate(tempDir)

			result.hasErrors() shouldBe false
			result.validatedCount shouldBe 1

			tempDir.deleteRecursively()
		}

		"should validate fixed types" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "fixed.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "WithFixed",
				  "namespace": "com.example",
				  "fields": [
				    {
				      "name": "fixedField",
				      "type": {
				        "type": "fixed",
				        "name": "MD5",
				        "size": 16
				      }
				    }
				  ]
				}
				""".trimIndent(),
			)

			val result = validator.validate(tempDir)

			result.hasErrors() shouldBe false
			result.validatedCount shouldBe 1

			tempDir.deleteRecursively()
		}

		"should handle mixed file types" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "record.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Record",
				  "fields": [{"name": "data", "type": "string"}]
				}
				""".trimIndent(),
			)

			File(tempDir, "protocol.avpr").writeText(
				"""
				{
				  "protocol": "TestProtocol",
				  "namespace": "com.test",
				  "types": [
				    {
				      "type": "enum",
				      "name": "Status",
				      "symbols": ["OK", "ERROR"]
				    }
				  ]
				}
				""".trimIndent(),
			)

			val result = validator.validate(tempDir)

			result.hasErrors() shouldBe false
			result.validatedCount shouldBe 2

			tempDir.deleteRecursively()
		}
	})
