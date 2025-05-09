package io.github.martinsjavacode.avro

import io.github.martinsjavacode.avro.extension.AvroPluginExtension
import io.github.martinsjavacode.avro.task.AvroTask
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldHaveExtension
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

class AvroGradlePluginTest : FunSpec({

	test("should apply plugin and register extension and task") {
		// Create a test project
		val project = ProjectBuilder.builder().build()

		// Apply the plugin
		project.pluginManager.apply("io.github.martinsjavacode.avro-gradle-plugin")

		// Verify the extension is registered
		val extension = project.extensions.findByName("avro")
		extension.shouldNotBeNull()
		(extension is AvroPluginExtension).shouldBeTrue()

		// Verify the task is registered
		val task = project.tasks.findByName("generateAvroClasses")
		task.shouldNotBeNull()
		(task is AvroTask).shouldBeTrue()
	}

	test("should generate Avro classes from schema files with default settings") {
		val projectDir =
			createTempDirectory("avro-plugin-test").toFile()
				.apply {
					resolve("settings.gradle.kts").writeText("")
					resolve("build.gradle.kts").writeText(
						"""
						plugins {
						    id("io.github.martinsjavacode.avro-gradle-plugin")
						}
						""".trimIndent(),
					)

					val avroDir = resolve("src/main/resources/avro").apply { mkdirs() }

					val resourceDir = Path.of("src/test/resources/avro")
					Files.walk(resourceDir).forEach { path ->
						if (path.toFile().isFile && path.toString().endsWith(".avsc")) {
							path.toFile()
								.copyTo(avroDir.resolve(path.fileName.toString()), overwrite = true)
						}
					}
				}

		// Executa o Gradle usando o TestKit
		GradleRunner.create()
			.withProjectDir(projectDir)
			.withPluginClasspath()
			.withArguments("generateAvroClasses")
			.build()

		// Check if the output directory exists
		val outputDir = projectDir.resolve("build/generated/java")
		outputDir.shouldExist()
		outputDir.walkTopDown()
			.filter { it.isFile }
			.forEach { file ->
				file.shouldBeAFile()
				file.shouldHaveExtension("java")
			}
	}

	test("should generate Avro classes with custom settings") {
		val projectDir =
			createTempDirectory("avro-plugin-test").toFile()
				.apply {
					resolve("settings.gradle.kts").writeText("")
					resolve("build.gradle.kts").writeText(
						"""
						plugins {
						    id("io.github.martinsjavacode.avro-gradle-plugin")
						}

						avro {
						    sourceDir = "src/main/resources/schemas"
						    outputDir = "build/generated/avro"
						    optionalGetters = true
						}
						""".trimIndent(),
					)

					val avroDir = resolve("src/main/resources/schemas").apply { mkdirs() }

					val resourceDir = Path.of("src/test/resources/avro")
					Files.walk(resourceDir).forEach { path ->
						if (path.toFile().isFile && path.toString().endsWith(".avsc")) {
							path.toFile()
								.copyTo(avroDir.resolve(path.fileName.toString()), overwrite = true)
						}
					}
				}

		GradleRunner.create()
			.withProjectDir(projectDir)
			.withPluginClasspath()
			.withArguments("generateAvroClasses")
			.build()

		// Check if the output directory exists
		val outputDir = projectDir.resolve("build/generated/avro")
		outputDir.shouldExist()
		outputDir.walkTopDown()
			.filter { it.isFile }
			.forEach { file ->
				file.shouldBeAFile()
				file.shouldHaveExtension("java")

				val content = file.readText()
				content.shouldContain("Optional")
			}
	}

	test("should generate Avro classes from source subdirectories") {
		val projectDir =
			createTempDirectory("avro-plugin-test").toFile()
				.apply {
					resolve("settings.gradle.kts").writeText("")
					resolve("build.gradle.kts").writeText(
						"""
						plugins {
						    id("io.github.martinsjavacode.avro-gradle-plugin")
						}

						avro {
						    sourceDir = "src/main/resources/avro"
						    outputDir = "build/generated/java"
						}
						""".trimIndent(),
					)

					val avroDir = resolve("src/main/resources/avro/schemas/user").apply { mkdirs() }

					val resourceDir = Path.of("src/test/resources/avro")
					Files.walk(resourceDir).forEach { path ->
						if (path.toFile().isFile && path.toString().endsWith(".avsc")) {
							path.toFile()
								.copyTo(avroDir.resolve(path.fileName.toString()), overwrite = true)
						}
					}
				}

		GradleRunner.create()
			.withProjectDir(projectDir)
			.withPluginClasspath()
			.withArguments("generateAvroClasses")
			.build()

		// Check if the output directory exists
		val outputDir = projectDir.resolve("build/generated/java")
		outputDir.shouldExist()
		outputDir.walkTopDown()
			.filter { it.isFile }
			.forEach { file ->
				file.shouldBeAFile()
				file.shouldHaveExtension("java")
			}
	}
})
