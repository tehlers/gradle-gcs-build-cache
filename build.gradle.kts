import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.3.31"
    id("com.github.hierynomus.license") version "0.15.0"
    id("com.gradle.plugin-publish") version "0.10.1"
    `kotlin-dsl`
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "8.2.0"
}

group = "net.idlestate"
version = "1.0.1"

repositories {
    jcenter()
}

dependencies {
    implementation("com.google.cloud:google-cloud-storage:1.86.0")
    implementation(kotlin("stdlib-jdk8"))
}

ktlint {
    reporters.set(setOf(ReporterType.PLAIN, ReporterType.CHECKSTYLE))
}

gradlePlugin {
    plugins {
        create("gcsBuildCache") {
            id = "net.idlestate.gradle-gcs-build-cache"
            implementationClass = "net.idlestate.gradle.caching.GCSBuildCachePlugin"
            displayName = "GCS Build Cache"
            description = "A Gradle build cache implementation that uses Google Cloud Storage (GCS) to store the build artifacts. Since this is a settings plugin the build script snippets below won't work. Please consult the documentation at Github."
        }
    }
}

pluginBundle {
    website = "https://github.com/tehlers/gradle-gcs-build-cache"
    vcsUrl = "https://github.com/tehlers/gradle-gcs-build-cache.git"
    tags = listOf("build-cache", "gcs", "Google Cloud Storage", "cache")
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}
