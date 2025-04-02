package io.github.martinsjavacode.avro

open class AvroPluginExtension {
    var group: String? = null
    var description: String? = null
    var sourceDir: String? = null
    var outputDir: String? = null
    var fieldVisibility: String = "PUBLIC"
    var stringType: String = "String"
    var optionalGetters: Boolean = false
}
