package io.github.martinsjavacode.avro

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.github.martinsjavacode.avro.task.AvroTask
import io.github.martinsjavacode.avro.task.ValidateAvroSchemasTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class AvroGradlePlugin : Plugin<Project> {
	override fun apply(project: Project) {
		val extension = project.extensions.create("avro", AvroPluginExtension::class.java)

		val validateTask =
			project.tasks.register("validateAvroSchemas", ValidateAvroSchemasTask::class.java) {
				group = "verification"
				description = "Validates Avro schema files"
				sourceDir.set(project.layout.projectDirectory.dir(extension.sourceDir))
				reportDir.set(project.layout.buildDirectory.dir("reports/avro-validation"))
				onlyIf { extension.validateBeforeGenerate.get() }
			}

		val generateTask =
			project.tasks.register("generateAvroClasses", AvroTask::class.java) {
				group = "build"
				description = "Generates Java classes from Avro schemas"
				sourceDir.set(project.layout.projectDirectory.dir(extension.sourceDir))
				outputDir.set(project.layout.buildDirectory.dir(extension.outputDir))
				reportDir.set(project.layout.buildDirectory.dir("reports/avro"))
				fieldVisibility.set(extension.fieldVisibility)
				stringType.set(extension.stringType)
				optionalGetters.set(extension.optionalGetters)
				useDecimalLogical.set(extension.useDecimalLogical)
				createNullSafeAnnotations.set(extension.createNullSafeAnnotations)
				dependsOn(validateTask)
			}

		project.plugins.withId("java") {
			project.tasks.named("compileJava") {
				dependsOn(generateTask)
			}
		}
	}
}
