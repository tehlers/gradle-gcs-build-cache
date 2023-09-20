import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.7.10"
    id("com.github.hierynomus.license") version "0.16.1"
    id("com.gradle.plugin-publish") version "1.0.0"
    `kotlin-dsl`
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = "net.idlestate"
version = "1.3.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.cloud:google-cloud-storage:2.17.1")
    implementation(kotlin("stdlib-jdk8"))
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
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

publishing.publications.configureEach {
    (this as MavenPublication).pom {
        scm {
            connection.set("scm:git:git@github.com:tehlers/gradle-gcs-build-cache.git")
            developerConnection.set("scm:git:git@github.com:tehlers/gradle-gcs-build-cache.git")
            url.set("https://github.com/tehlers/gradle-gcs-build-cache/")
        }
        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://spdx.org/licenses/Apache-2.0.html")
            }
        }
    }
}
