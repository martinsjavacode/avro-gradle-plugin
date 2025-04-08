plugins {
    java
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("idea")
    id("com.gradle.plugin-publish")
    id("org.jlleitschuh.gradle.ktlint")
    jacoco
}

group = "io.github.martinsjavacode"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.apache.avro:avro:${property("avroVersion")}")
    implementation("org.apache.avro:avro-compiler:${property("avroVersion")}")

    testImplementation(gradleTestKit())
    testImplementation("io.kotest:kotest-runner-junit5:${property("kotestVersion")}")
    testImplementation("io.kotest:kotest-assertions-core:${property("kotestVersion")}")
    testImplementation("io.mockk:mockk:${property("mockkVersion")}")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

gradlePlugin {
    website = "https://github.com/martinsjavacode/avro-gradle-plugin"
    vcsUrl = "https://github.com/martinsjavacode/avro-gradle-plugin.git"

    plugins {
        create("avro") {
            id = "io.github.martinsjavacode.avro-gradle-plugin"
            implementationClass = "io.github.martinsjavacode.avro.AvroGradlePlugin"
            displayName = "Avro Gradle Plugin"
            description = "A Gradle plugin to allow easily performing Java code generation for Apache Avro."
            tags.set(listOf("avro", "generator", "serialization", "java", "avsc"))
        }
    }
}

jacoco {
    toolVersion = "${property("jacocoVersion")}"
    reportsDirectory = layout.buildDirectory.dir("reports/jacoco")
}

val excludeRulesDir =
    fileTree(
        layout.buildDirectory.dir("classes/kotlin/main")
    ) {
        exclude(
            "**/extension/*",
            "**/generated/*",
        )
    }

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("jacoco/test/html"))
    }

    classDirectories.setFrom(excludeRulesDir)
}

tasks.jacocoTestCoverageVerification {
    classDirectories.setFrom(excludeRulesDir)

    violationRules {
        rule {
            limit {
                minimum = BigDecimal.valueOf(0.9)
            }
        }
    }

    dependsOn(tasks.jacocoTestReport)
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
