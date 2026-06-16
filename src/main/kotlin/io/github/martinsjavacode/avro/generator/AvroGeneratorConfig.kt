package io.github.martinsjavacode.avro.generator

data class AvroGeneratorConfig(
	val fieldVisibility: String,
	val stringType: String,
	val optionalGetters: Boolean,
	val useDecimalLogical: Boolean,
	val createNullSafeAnnotations: Boolean,
)
