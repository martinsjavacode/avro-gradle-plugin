package io.github.martinsjavacode.avro.extension

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder

class AvroPluginExtensionTest :
	StringSpec({
		"should have default values" {
			val project = ProjectBuilder.builder().build()
			val extension = project.extensions.create("avro", AvroPluginExtension::class.java)

			extension.sourceDir.get() shouldBe "src/main/resources/avro"
			extension.outputDir.get() shouldBe "generated/java"
			extension.fieldVisibility.get() shouldBe "PUBLIC"
			extension.stringType.get() shouldBe "String"
			extension.optionalGetters.get() shouldBe false
			extension.useDecimalLogical.get() shouldBe false
			extension.createNullSafeAnnotations.get() shouldBe false
			extension.validateBeforeGenerate.get() shouldBe true
		}

		"should allow setting custom values" {
			val project = ProjectBuilder.builder().build()
			val extension = project.extensions.create("avro", AvroPluginExtension::class.java)

			extension.sourceDir.set("custom/source")
			extension.outputDir.set("custom/output")
			extension.fieldVisibility.set("PRIVATE")
			extension.stringType.set("CharSequence")
			extension.optionalGetters.set(true)
			extension.useDecimalLogical.set(true)
			extension.createNullSafeAnnotations.set(true)
			extension.validateBeforeGenerate.set(false)

			extension.sourceDir.get() shouldBe "custom/source"
			extension.outputDir.get() shouldBe "custom/output"
			extension.fieldVisibility.get() shouldBe "PRIVATE"
			extension.stringType.get() shouldBe "CharSequence"
			extension.optionalGetters.get() shouldBe true
			extension.useDecimalLogical.get() shouldBe true
			extension.createNullSafeAnnotations.get() shouldBe true
			extension.validateBeforeGenerate.get() shouldBe false
		}

		"should support all string types" {
			val project = ProjectBuilder.builder().build()
			val extension = project.extensions.create("avro", AvroPluginExtension::class.java)

			extension.stringType.set("String")
			extension.stringType.get() shouldBe "String"

			extension.stringType.set("CharSequence")
			extension.stringType.get() shouldBe "CharSequence"

			extension.stringType.set("Utf8")
			extension.stringType.get() shouldBe "Utf8"
		}

		"should support all field visibility options" {
			val project = ProjectBuilder.builder().build()
			val extension = project.extensions.create("avro", AvroPluginExtension::class.java)

			extension.fieldVisibility.set("PUBLIC")
			extension.fieldVisibility.get() shouldBe "PUBLIC"

			extension.fieldVisibility.set("PRIVATE")
			extension.fieldVisibility.get() shouldBe "PRIVATE"
		}
	})
