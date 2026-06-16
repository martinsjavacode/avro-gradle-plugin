package io.github.martinsjavacode.avro.extension

import org.gradle.api.provider.Property

abstract class AvroPluginExtension {
	abstract val sourceDir: Property<String>
	abstract val outputDir: Property<String>
	abstract val fieldVisibility: Property<String>
	abstract val stringType: Property<String>
	abstract val optionalGetters: Property<Boolean>
	abstract val useDecimalLogical: Property<Boolean>
	abstract val createNullSafeAnnotations: Property<Boolean>
	abstract val validateBeforeGenerate: Property<Boolean>

	init {
		sourceDir.convention("src/main/resources/avro")
		outputDir.convention("generated/java")
		fieldVisibility.convention("PUBLIC")
		stringType.convention("String")
		optionalGetters.convention(false)
		useDecimalLogical.convention(false)
		createNullSafeAnnotations.convention(false)
		validateBeforeGenerate.convention(true)
	}
}
