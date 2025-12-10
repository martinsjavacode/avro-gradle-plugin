package io.github.martinsjavacode.avro.validator

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File
import kotlin.io.path.createTempDirectory

class SchemaValidatorTest :
	StringSpec({
		"should validate valid AVSC schema" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory().toFile()

			File(tempDir, "user.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "User",
				  "namespace": "com.example",
				  "fields": [
				    {"name": "name", "type": "string"},
				    {"name": "age", "type": "int"}
				  ]
				}
				""".trimIndent(),
			)

			val result = validator.validate(tempDir)

			result.hasErrors() shouldBe false
			result.validatedCount shouldBe 1

			tempDir.deleteRecursively()
		}

		"should detect invalid schema with no fields" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "empty.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Empty",
				  "namespace": "com.example",
				  "fields": []
				}
				""".trimIndent(),
			)

			val result = validator.validate(tempDir)

			result.hasErrors() shouldBe true
			result.errors.size shouldBe 1

			tempDir.deleteRecursively()
		}

		"should validate multiple schema files" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "user.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "User",
				  "fields": [{"name": "name", "type": "string"}]
				}
				""".trimIndent(),
			)

			File(tempDir, "product.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Product",
				  "fields": [{"name": "id", "type": "long"}]
				}
				""".trimIndent(),
			)

			val result = validator.validate(tempDir)

			result.hasErrors() shouldBe false
			result.validatedCount shouldBe 2

			tempDir.deleteRecursively()
		}

		"should validate AVPR protocol files" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "protocol.avpr").writeText(
				"""
				{
				  "protocol": "TestProtocol",
				  "namespace": "com.example",
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

			val result = validator.validate(tempDir)

			result.hasErrors() shouldBe false
			result.validatedCount shouldBe 1

			tempDir.deleteRecursively()
		}

		"should validate enum schemas" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "status.avsc").writeText(
				"""
				{
				  "type": "enum",
				  "name": "Status",
				  "namespace": "com.example",
				  "symbols": ["ACTIVE", "INACTIVE"]
				}
				""".trimIndent(),
			)

			val result = validator.validate(tempDir)

			result.hasErrors() shouldBe false
			result.validatedCount shouldBe 1

			tempDir.deleteRecursively()
		}

		"should detect enum with no symbols" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "empty-enum.avsc").writeText(
				"""
				{
				  "type": "enum",
				  "name": "EmptyEnum",
				  "namespace": "com.example",
				  "symbols": []
				}
				""".trimIndent(),
			)

			val result = validator.validate(tempDir)

			result.hasErrors() shouldBe true

			tempDir.deleteRecursively()
		}

		"should validate nested record types" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "nested.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Order",
				  "namespace": "com.example",
				  "fields": [
				    {
				      "name": "customer",
				      "type": {
				        "type": "record",
				        "name": "Customer",
				        "fields": [
				          {"name": "name", "type": "string"}
				        ]
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

		"should validate array types" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "array.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Container",
				  "namespace": "com.example",
				  "fields": [
				    {
				      "name": "items",
				      "type": {
				        "type": "array",
				        "items": "string"
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

		"should validate map types" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "map.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Config",
				  "namespace": "com.example",
				  "fields": [
				    {
				      "name": "properties",
				      "type": {
				        "type": "map",
				        "values": "string"
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

		"should validate union types" {
			val project = mockk<Project>()
			val logger = mockk<Logger>(relaxed = true)
			every { project.logger } returns logger

			val validator = SchemaValidator(project)
			val tempDir = createTempDirectory("test").toFile()

			File(tempDir, "union.avsc").writeText(
				"""
				{
				  "type": "record",
				  "name": "Optional",
				  "namespace": "com.example",
				  "fields": [
				    {
				      "name": "value",
				      "type": ["null", "string"]
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
	})
