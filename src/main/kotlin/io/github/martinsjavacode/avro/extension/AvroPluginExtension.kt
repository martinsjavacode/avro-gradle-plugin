package io.github.martinsjavacode.avro.extension

open class AvroPluginExtension {
	var sourceDir: String? = null
	var outputDir: String? = null
	var fieldVisibility: String = "PUBLIC"
	var stringType: String = "String"
	var optionalGetters: Boolean = false
	var useDecimalLogical: Boolean = false
	var createNullSafeAnnotations: Boolean = false
	
	// Novas opções
	var enableCache: Boolean = true
	var validateBeforeGenerate: Boolean = true
	var generateBuilders: Boolean = false
	var addCustomHeader: String? = null
	var logicalTypeConversions: Map<String, String> = emptyMap()
}
