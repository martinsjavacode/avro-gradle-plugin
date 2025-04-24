pluginManagement {

    val gradlePluginVersion: String by settings
    val ktlintVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("idea")
        `java-gradle-plugin`
        `kotlin-dsl`
        `maven-publish`
        id("com.gradle.plugin-publish") version gradlePluginVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
        jacoco
    }
}

rootProject.name = "avro-gradle-plugin"
