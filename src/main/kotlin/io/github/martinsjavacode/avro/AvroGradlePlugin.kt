package io.github.martinsjavacode.avro

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.github.martinsjavacode.avro.task.AvroTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class AvroGradlePlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.extensions
			.create("avro", AvroPluginExtension::class.java)

		project.tasks
			.register("generateAvroClasses", AvroTask::class.java)
	}
}
