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
		val reportFile = File(outputDir, "avro-generation-report.html")

		val html =
			"""
			<!DOCTYPE html>
			<html>
			<head>
			    <meta charset="UTF-8">
			    <title>Avro Generation Report</title>
			    <style>
			        body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
			        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
			        h1 { color: #333; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }
			        .summary { background: #e8f5e9; padding: 15px; border-radius: 4px; margin: 20px 0; }
			        .summary-item { display: inline-block; margin-right: 30px; }
			        .summary-label { font-weight: bold; color: #2e7d32; }
			        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
			        th { background: #4CAF50; color: white; padding: 12px; text-align: left; }
			        td { padding: 10px; border-bottom: 1px solid #ddd; }
			        tr:hover { background: #f5f5f5; }
			        .timestamp { color: #666; font-size: 0.9em; }
			    </style>
			</head>
			<body>
			    <div class="container">
			        <h1>Avro Generation Report</h1>
			        <div class="timestamp">Generated: ${
				LocalDateTime.now().format(
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
				)
			}</div>

					<div class="summary">
						<div class="summary-item">
							<span class="summary-label">Total Classes:</span> ${classes.size}
						</div>
						<div class="summary-item">
							<span class="summary-label">Duration:</span> ${duration}ms
						</div>
						<div class="summary-item">
							<span class="summary-label">Status:</span> ✓ Success
						</div>
					</div>

					<table>
						<thead>
							<tr>
								<th>Class Name</th>
								<th>Type</th>
								<th>Source File</th>
								<th>Output File</th>
							</tr>
						</thead>
						<tbody>
							${
				classes.joinToString("\n") {
					"""
					<tr>
						<td>${it.name}</td>
						<td>${it.type}</td>
						<td>${it.sourceFile}</td>
						<td>${it.outputFile}</td>
					</tr>
					""".trimIndent()
				}
			}
						</tbody>
					</table>
			    </div>
			</body>
			</html>
			""".trimIndent()

		reportFile.writeText(html)
	}

	fun getClassCount(): Int = classes.size

	fun getDuration(): Long = System.currentTimeMillis() - startTime
}
