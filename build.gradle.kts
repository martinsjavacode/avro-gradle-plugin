plugins {
    java
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("idea")
    id("com.gradle.plugin-publish")
    id("org.jlleitschuh.gradle.ktlint")
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
}

gradlePlugin {
    plugins {
        create("avro") {
            id = "io.github.martinsjavacode.avro-gradle-plugin"
            implementationClass = "io.github.martinsjavacode.avro.AvroGradlePlugin"
            displayName = "Avro Gradle Plugin"
            description = "A Gradle plugin to allow easily performing Java code generation for Apache Avro."
            tags.set(listOf("avro", "generator", "gradle", "serialization", "java", "avsc"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            group = project.group
            version = project.version.toString()
            artifactId = project.name
        }
    }
    repositories {
        maven {
            name = "GradlePluginPortal"
            url = uri("https://plugins.gradle.org/m2/")
            credentials {
                username = findProperty("plugin.portal.username")?.toString()
                    ?: System.getenv("GRADLE_PLUGIN_PORTAL_USERNAME")
                password =
                    findProperty("plugin.portal.password")?.toString()
                        ?: System.getenv("GRADLE_PLUGIN_PORTAL_PASSWORD")
            }
        }

        maven {
            name = "NexusLocal"
            url = uri("http://localhost:8000/repository/maven-snapshots")
            isAllowInsecureProtocol = true
            credentials {
                username = "admin"
                password = "123124"
            }
            version = "1.0.0-SNAPSHOT"
        }
    }
}
