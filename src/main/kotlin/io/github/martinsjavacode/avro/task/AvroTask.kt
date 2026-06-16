package io.github.martinsjavacode.avro.task

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.github.martinsjavacode.avro.generator.AvroGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

@CacheableTask
abstract class AvroTask : DefaultTask() {
	@get:Input
	abstract val fieldVisibility: Property<String>

	@get:Input
	abstract val stringType: Property<String>

	@get:Input
	abstract val optionalGetters: Property<Boolean>

	@get:Input
	abstract val useDecimalLogical: Property<Boolean>

	@get:Input
	abstract val createNullSafeAnnotations: Property<Boolean>

	@get:InputDirectory
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val sourceDir: DirectoryProperty

	@get:OutputDirectory
	abstract val outputDir: DirectoryProperty

	@get:InputFiles
	@get:PathSensitive(PathSensitivity.RELATIVE)
	val schemaFiles: FileTree
		get() = sourceDir.asFileTree.matching { include("**/*.avsc", "**/*.avpr") }

	@TaskAction
	fun generateAvroClasses() {
		val source = sourceDir.get().asFile
		val output = outputDir.get().asFile
		output.mkdirs()

		if (!source.exists()) {
			logger.lifecycle("Source directory does not exist: $source")
			return
		}

		val extension =
			AvroPluginExtension().apply {
				fieldVisibility = this@AvroTask.fieldVisibility.get()
				stringType = this@AvroTask.stringType.get()
				optionalGetters = this@AvroTask.optionalGetters.get()
				useDecimalLogical = this@AvroTask.useDecimalLogical.get()
				createNullSafeAnnotations = this@AvroTask.createNullSafeAnnotations.get()
			}

		AvroGenerator.process(
			sourceDir = source,
			outputDirectory = output,
			extension = extension,
			reportDir = project.layout.buildDirectory.dir("reports/avro").get().asFile,
			logger = logger,
		)

		logger.lifecycle("Avro classes generated successfully")
	}
}
