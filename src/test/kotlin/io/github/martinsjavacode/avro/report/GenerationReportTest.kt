package io.github.martinsjavacode.avro.report

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import kotlin.io.path.createTempDirectory

class GenerationReportTest :
	StringSpec({
		"should track class count correctly" {
			val report = GenerationReport()

			report.getClassCount() shouldBe 0

			report.addClass(
				GeneratedClass(
					name = "User",
					sourceFile = "user.avsc",
					outputFile = "com/example/User.java",
					type = "AVSC",
				),
			)

			report.getClassCount() shouldBe 1

			report.addClass(
				GeneratedClass("Order", "order.avsc", "Order.java", "AVSC"),
			)

			report.getClassCount() shouldBe 2
		}

		"should generate valid HTML report" {
			val report = GenerationReport()
			val outputDir = createTempDirectory("report-test").toFile()

			report.addClass(
				GeneratedClass("User", "user.avsc", "com/example/User.java", "AVSC"),
			)
			report.addClass(
				GeneratedClass("Order", "order.avpr", "com/shop/Order.java", "AVPR"),
			)

			report.generateHtmlReport(outputDir)

			val reportFile = File(outputDir, "avro-generation-report.html")
			reportFile.exists() shouldBe true

			val content = reportFile.readText()
			
			// Verificar estrutura HTML
			content shouldContain "<!DOCTYPE html>"
			content shouldContain "<html>"
			content shouldContain "</html>"
			content shouldContain "Avro Generation Report"
			
			// Verificar dados
			content shouldContain "User"
			content shouldContain "Order"
			content shouldContain "user.avsc"
			content shouldContain "order.avpr"
			content shouldContain "AVSC"
			content shouldContain "AVPR"
			content shouldContain "com/example/User.java"
			content shouldContain "com/shop/Order.java"
			
			// Verificar estatísticas
			content shouldContain "Total Classes:"
			content shouldContain "2"
			content shouldContain "Duration:"
			content shouldContain "Status:"

			outputDir.deleteRecursively()
		}

		"should track duration" {
			val report = GenerationReport()

			Thread.sleep(50)

			val duration = report.getDuration()
			duration shouldBeGreaterThan 40L
		}

		"should generate report with empty classes list" {
			val report = GenerationReport()
			val outputDir = createTempDirectory("report-test").toFile()

			report.generateHtmlReport(outputDir)

			val reportFile = File(outputDir, "avro-generation-report.html")
			reportFile.exists() shouldBe true

			val content = reportFile.readText()
			content shouldContain "Total Classes:</span> 0"

			outputDir.deleteRecursively()
		}

		"should handle multiple schema types in report" {
			val report = GenerationReport()
			val outputDir = createTempDirectory("report-test").toFile()

			report.addClass(GeneratedClass("User", "user.avsc", "User.java", "AVSC"))
			report.addClass(GeneratedClass("Protocol", "proto.avpr", "Protocol.java", "AVPR"))

			report.generateHtmlReport(outputDir)

			val content = File(outputDir, "avro-generation-report.html").readText()
			
			content shouldContain "AVSC"
			content shouldContain "AVPR"

			outputDir.deleteRecursively()
		}
	})
