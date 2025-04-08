package io.github.martinsjavacode.avro.generator

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import org.apache.avro.Schema
import org.apache.avro.compiler.specific.SpecificCompiler
import org.apache.avro.compiler.specific.SpecificCompiler.FieldVisibility
import org.apache.avro.generic.GenericData.StringType
import org.gradle.api.Project
import java.io.File

object AvroGenerator {
    fun generate(project: Project, extension: AvroPluginExtension, sourceDirectory: File, outputDirectory: File) {
        sourceDirectory.listFiles { file -> file.extension == "avsc" }?.forEach { file ->
            val schema = Schema.Parser().parse(file)
            val compiler = SpecificCompiler(schema).apply {
                isCreateOptionalGetters = extension.optionalGetters
                isCreateSetters = extension.fieldVisibility == "PUBLIC"
                isCreateNullSafeAnnotations = extension.createNullSafeAnnotations
                setFieldVisibility(FieldVisibility.valueOf(extension.fieldVisibility))
                setStringType(StringType.valueOf(extension.stringType))
                setEnableDecimalLogicalType(extension.useDecimalLogical)
            }

            val output = compiler.compileToDestination(file, outputDirectory)
            project.logger.lifecycle("Generated class: $output")
        }
    }
}
