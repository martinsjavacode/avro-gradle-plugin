package io.github.martinsjavacode.avro.extension

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AvroPluginExtensionTest :
	StringSpec({
		"should have default values" {
			val extension = AvroPluginExtension()

			extension.sourceDir shouldBe null
			extension.outputDir shouldBe null
			extension.fieldVisibility shouldBe "PUBLIC"
			extension.stringType shouldBe "String"
			extension.optionalGetters shouldBe false
			extension.useDecimalLogical shouldBe false
			extension.createNullSafeAnnotations shouldBe false
			extension.validateBeforeGenerate shouldBe true
		}

		"should allow setting custom values" {
			val extension = AvroPluginExtension()

			extension.sourceDir = "custom/source"
			extension.outputDir = "custom/output"
			extension.fieldVisibility = "PRIVATE"
			extension.stringType = "CharSequence"
			extension.optionalGetters = true
			extension.useDecimalLogical = true
			extension.createNullSafeAnnotations = true
			extension.validateBeforeGenerate = false

			extension.sourceDir shouldBe "custom/source"
			extension.outputDir shouldBe "custom/output"
			extension.fieldVisibility shouldBe "PRIVATE"
			extension.stringType shouldBe "CharSequence"
			extension.optionalGetters shouldBe true
			extension.useDecimalLogical shouldBe true
			extension.createNullSafeAnnotations shouldBe true
			extension.validateBeforeGenerate shouldBe false
		}

		"should support all string types" {
			val extension = AvroPluginExtension()

			extension.stringType = "String"
			extension.stringType shouldBe "String"

			extension.stringType = "CharSequence"
			extension.stringType shouldBe "CharSequence"

			extension.stringType = "Utf8"
			extension.stringType shouldBe "Utf8"
		}

		"should support all field visibility options" {
			val extension = AvroPluginExtension()

			extension.fieldVisibility = "PUBLIC"
			extension.fieldVisibility shouldBe "PUBLIC"

			extension.fieldVisibility = "PRIVATE"
			extension.fieldVisibility shouldBe "PRIVATE"
		}
	})
