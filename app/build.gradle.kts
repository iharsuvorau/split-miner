/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.2.1/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application

    id("com.github.johnrengelman.shadow") version "8.1.1"
}

version = "1.4"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()

    flatDir {
        dirs("libs")
    }
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:31.1-jre")

    implementation(
        fileTree("../lib") {
            include("*.jar")
        }
    )
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

application {
    // Define the main class for the application.
    mainClass.set("splitminer.App")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.named<Jar>("shadowJar") {
    manifest {
        attributes["Main-Class"] = "splitminer.App"
    }
    archiveBaseName.set("splitminer")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}