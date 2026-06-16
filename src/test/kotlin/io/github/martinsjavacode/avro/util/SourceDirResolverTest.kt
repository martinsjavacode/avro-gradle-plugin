package io.github.martinsjavacode.avro.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import java.io.File
import kotlin.io.path.createTempDirectory

class SourceDirResolverTest :
	StringSpec({
		"should return default dir when no config files exist" {
			val projectDir = createTempDirectory("project").toFile()
			val defaultDir = File(projectDir, "src/main/resources/avro")
			val project =
				mockk<Project> {
					every { file("application.properties") } returns File(projectDir, "application.properties")
					every { file("application.yml") } returns File(projectDir, "application.yml")
				}

			val result = SourceDirResolver.resolve(project, defaultDir)

			result shouldBe defaultDir
			projectDir.deleteRecursively()
		}

		"should read sourceDirectory from application.properties" {
			val projectDir = createTempDirectory("project").toFile()
			val defaultDir = File(projectDir, "src/main/resources/avro")
			val customDir = File(projectDir, "custom/avro")

			File(projectDir, "application.properties").writeText(
				"sourceDirectory=${customDir.absolutePath}",
			)

			val project =
				mockk<Project> {
					every { file("application.properties") } returns File(projectDir, "application.properties")
					every { file("application.yml") } returns File(projectDir, "application.yml")
					every { file(customDir.absolutePath) } returns customDir
				}

			val result = SourceDirResolver.resolve(project, defaultDir)

			result shouldBe customDir
			projectDir.deleteRecursively()
		}

		"should read sourceDirectory from application.yml" {
			val projectDir = createTempDirectory("project").toFile()
			val defaultDir = File(projectDir, "src/main/resources/avro")
			val customDir = File(projectDir, "yaml-avro")

			File(projectDir, "application.yml").writeText(
				"sourceDirectory: ${customDir.absolutePath}",
			)

			val project =
				mockk<Project> {
					every { file("application.properties") } returns File(projectDir, "application.properties")
					every { file("application.yml") } returns File(projectDir, "application.yml")
					every { file(customDir.absolutePath) } returns customDir
				}

			val result = SourceDirResolver.resolve(project, defaultDir)

			result shouldBe customDir
			projectDir.deleteRecursively()
		}

		"should return default when yaml has no sourceDirectory key" {
			val projectDir = createTempDirectory("project").toFile()
			val defaultDir = File(projectDir, "src/main/resources/avro")

			File(projectDir, "application.yml").writeText(
				"otherKey: someValue",
			)

			val project =
				mockk<Project> {
					every { file("application.properties") } returns File(projectDir, "application.properties")
					every { file("application.yml") } returns File(projectDir, "application.yml")
				}

			val result = SourceDirResolver.resolve(project, defaultDir)

			result shouldBe defaultDir
			projectDir.deleteRecursively()
		}

		"should prefer properties over yaml" {
			val projectDir = createTempDirectory("project").toFile()
			val defaultDir = File(projectDir, "src/main/resources/avro")
			val propsDir = File(projectDir, "props-dir")
			val yamlDir = File(projectDir, "yaml-dir")

			File(projectDir, "application.properties").writeText(
				"sourceDirectory=${propsDir.absolutePath}",
			)
			File(projectDir, "application.yml").writeText(
				"sourceDirectory: ${yamlDir.absolutePath}",
			)

			val project =
				mockk<Project> {
					every { file("application.properties") } returns File(projectDir, "application.properties")
					every { file("application.yml") } returns File(projectDir, "application.yml")
					every { file(propsDir.absolutePath) } returns propsDir
				}

			val result = SourceDirResolver.resolve(project, defaultDir)

			result shouldBe propsDir
			projectDir.deleteRecursively()
		}
	})
