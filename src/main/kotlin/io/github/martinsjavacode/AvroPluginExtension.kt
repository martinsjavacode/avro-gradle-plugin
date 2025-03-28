package io.github.martinsjavacode

open class AvroPluginExtension {
    var sourceDir: String? = null
    var outputDir: String? = null
    var group: String? = null
    var description: String? = null
    var fieldVisibility: String = "PUBLIC"
    var stringType: String = "String"
    var optionalGetters: Boolean = false
}
