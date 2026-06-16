package io.github.martinsjavacode.avro.report

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class GeneratedClass(
	val name: String,
	val sourceFile: String,
	val outputFile: String,
	val type: String,
)

class GenerationReport {
	private val classes = mutableListOf<GeneratedClass>()
	private val startTime = System.currentTimeMillis()

	fun addClass(generatedClass: GeneratedClass) {
		classes.add(generatedClass)
	}

	fun generateHtmlReport(outputDir: File) {
		val duration = System.currentTimeMillis() - startTime
		val template = javaClass.getResource("/templates/avro-generation-report.html")!!.readText()

		val rowsHtml =
			classes.joinToString("\n") {
				"                <tr><td>${it.name}</td><td>${it.type}</td><td>${it.sourceFile}</td><td>${it.outputFile}</td></tr>"
			}

		val html =
			template
				.replace("{{timestamp}}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
				.replace("{{totalClasses}}", classes.size.toString())
				.replace("{{duration}}", duration.toString())
				.replace("{{rows}}", rowsHtml)

		File(outputDir, "avro-generation-report.html").writeText(html)
	}

	fun getClassCount(): Int = classes.size

	fun getDuration(): Long = System.currentTimeMillis() - startTime
}
