package io.github.martinsjavacode.avro.task

import io.kotest.core.spec.style.FunSpec
import org.gradle.api.Project
import java.io.File

class AvroTaskTest : FunSpec({
	lateinit var project: Project
	lateinit var sourceDirectory: File
	lateinit var outputDirectory: File
})
