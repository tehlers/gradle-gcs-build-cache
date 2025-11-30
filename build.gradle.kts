import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.ktlint)
}

group = "net.idlestate"
version = "1.4.0"

dependencies {
    implementation(libs.google.cloud.storage)
}

ktlint {
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    website = "https://github.com/tehlers/gradle-gcs-build-cache"
    vcsUrl = "https://github.com/tehlers/gradle-gcs-build-cache.git"

    plugins {
        create(
            "gcsBuildCache",
            Action {
                id = "net.idlestate.gradle-gcs-build-cache"
                implementationClass = "net.idlestate.gradle.caching.GCSBuildCachePlugin"
                displayName = "GCS Build Cache"
                description =
                    "A Gradle build cache implementation that uses Google Cloud Storage (GCS) to store the build artifacts. Since this is a settings plugin the build script snippets below won't work. Please consult the documentation at Github."
                tags = listOf("build-cache", "gcs", "Google Cloud Storage", "cache")
            },
        )
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = "GCS Build Cache"
            description = "A Gradle build cache implementation that uses Google Cloud Storage (GCS) to store the build artifacts."
            scm {
                connection = "scm:git:git@github.com:tehlers/gradle-gcs-build-cache.git"
                developerConnection = "scm:git:git@github.com:tehlers/gradle-gcs-build-cache.git"
                url = "https://github.com/tehlers/gradle-gcs-build-cache/"
            }
            licenses {
                license {
                    name = "Apache License 2.0"
                    url = "https://spdx.org/licenses/Apache-2.0.html"
                    distribution = "repo"
                }
            }
        }
    }
}

tasks.wrapper {
    gradleVersion = "9.2.1"
    distributionType = Wrapper.DistributionType.ALL
}
