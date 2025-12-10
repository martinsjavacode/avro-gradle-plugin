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
			}

		val generateTask =
			project.tasks.register("generateAvroClasses", AvroTask::class.java) {
				group = "build"
				description = "Generates Java classes from Avro schemas"
			}

		project.afterEvaluate {
			if (extension.validateBeforeGenerate) {
				generateTask.configure {
					dependsOn(validateTask)
				}
			}

			project.tasks.findByName("compileJava")?.dependsOn(generateTask)
		}
	}
}
