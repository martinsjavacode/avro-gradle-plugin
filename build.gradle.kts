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

gradlePlugin {
    plugins {
        create("avroPlugin") {
            id = "io.github.martinsjavacode.avro-gradle-plugin"
            implementationClass = "io.github.martinsjavacode.AvroGradlePlugin"
            displayName = "Avro Plugin for Gradle"
            description = "A Gradle plugin to allow easily performing Java code generation for Apache Avro."
            tags.set(listOf("avro", "generator", "gradle", "serialization", "java", "avsc"))
        }
    }
}

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

// publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            from(components["java"])
//            groupId = project.group.toString()
//            artifactId = project.name
//            version = project.version.toString()
//        }
//    }
//    repositories {
//        maven {
//            name = "GradlePluginPortal"
//            url = uri("https://plugins.gradle.org/m2/")
//            credentials {
//                username = findProperty("plugin.portal.username") ?: System.getenv("GRADLE_PLUGIN_PORTAL_USERNAME")
//                password = findProperty("plugin.portal.password") ?: System.getenv("GRADLE_PLUGIN_PORTAL_PASSWORD")
//            }
//        }
//    }
// }
