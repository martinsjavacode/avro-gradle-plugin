package io.github.martinsjavacode.avro.task

import io.github.martinsjavacode.avro.generator.AvroGenerator
import io.github.martinsjavacode.avro.generator.AvroGeneratorConfig
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

	@get:Internal
	abstract val reportDir: DirectoryProperty

	@get:InputFiles
	@get:PathSensitive(PathSensitivity.RELATIVE)
	val schemaFiles: FileTree
		get() = sourceDir.asFileTree.matching { include("**/*.avsc", "**/*.avpr", "**/*.avdl") }

	@TaskAction
	fun generateAvroClasses() {
		val source = sourceDir.get().asFile
		val output = outputDir.get().asFile
		output.mkdirs()

		if (!source.exists()) {
			logger.lifecycle("Source directory does not exist: $source")
			return
		}

		val config =
			AvroGeneratorConfig(
				fieldVisibility = fieldVisibility.get(),
				stringType = stringType.get(),
				optionalGetters = optionalGetters.get(),
				useDecimalLogical = useDecimalLogical.get(),
				createNullSafeAnnotations = createNullSafeAnnotations.get(),
			)

		AvroGenerator.process(
			sourceDir = source,
			outputDirectory = output,
			config = config,
			reportDir = reportDir.get().asFile,
			logger = logger,
		)

		logger.lifecycle("Avro classes generated successfully")
	}
}
