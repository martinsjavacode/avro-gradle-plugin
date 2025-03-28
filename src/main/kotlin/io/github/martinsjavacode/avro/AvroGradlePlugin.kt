package io.github.martinsjavacode.avro

import org.apache.avro.Schema
import org.apache.avro.compiler.specific.SpecificCompiler
import org.apache.avro.compiler.specific.SpecificCompiler.FieldVisibility
import org.apache.avro.generic.GenericData.StringType
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.*

class AvroGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension =
            project.extensions
                .create("avro", AvroPluginExtension::class.java)

        project.tasks.register("generateAvroClasses") {
            group = extension.group ?: "build"
            description = extension.description ?: "Plugin to generate Avro classes from schema files in the project"

            doLast {
                val sourceDirectory =
                    extension.sourceDir?.let { project.file(it) }
                        ?: project.file("src/main/resources/avro")
                val outputDirectory =
                    extension.outputDir?.let { project.file(it) }
                        ?: project.file("build/generated/java")

                if (!outputDirectory.exists()) {
                    outputDirectory.mkdirs()
                }

                val propertyFile = project.file("application.properties")
                val yamlFile = project.file("application.yml")

                val customSourceDir =
                    when {
                        propertyFile.exists() -> {
                            Properties().apply { load(propertyFile.inputStream()) }
                                .getProperty("sourceDirectory")
                        }

                        yamlFile.exists() -> {
                            Properties().apply { load(yamlFile.inputStream()) }
                                .getProperty("sourceDirectory")
                        }

                        else -> null
                    }

                val finalSourceDir =
                    customSourceDir?.let {
                        project.file(it)
                    } ?: sourceDirectory

                if (!finalSourceDir.exists()) {
                    project.logger.lifecycle("Source directory does not exist: $finalSourceDir")
                    return@doLast
                }

                finalSourceDir.listFiles {
                    file -> file.isDirectory
                }?.forEach { subDir ->
                    val finalSubDir = File(finalSourceDir, subDir.name)
                    if (finalSubDir.exists()) {
                        project.logger.lifecycle("Using sub source directory: $finalSubDir")
                        generate(project, extension, finalSubDir, outputDirectory)
                    } else {
                        project.logger.lifecycle("Sub source directory does not exist: $finalSubDir")
                        return@doLast
                    }
                } ?: generate(project, extension, finalSourceDir, outputDirectory)

                project.logger.lifecycle("Avro classes generated successfully")
            }
        }
    }

    private fun generate(project: Project, extension: AvroPluginExtension, sourceDirectory: File, outputDirectory: File,) {
        sourceDirectory.listFiles { file ->
            file.extension == "avsc"
        }?.forEach { avscFile ->
            // Parse avsc file to avro schema
            val schema = Schema.Parser().parse(avscFile)
            val compiler =
                SpecificCompiler(schema).apply {
                    isCreateOptionalGetters = extension.optionalGetters
                    isCreateSetters = extension.fieldVisibility == "PUBLIC"
                    setFieldVisibility(FieldVisibility.valueOf(extension.fieldVisibility))
                    setStringType(StringType.valueOf(extension.stringType))
                }

            val output = compiler.compileToDestination(avscFile, outputDirectory)
            project.logger.lifecycle("Generated class: $output")
        }
    }
}
