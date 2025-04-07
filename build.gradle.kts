// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.notificare.services) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

subprojects {
    if (this.name != "sample" && this.name != "sample-user-inbox") {

        apply(plugin = "com.android.library")
        apply(plugin = "kotlin-android")
        apply(plugin = "com.google.devtools.ksp")
        apply(plugin = "kotlin-parcelize")
        apply(plugin = "maven-publish")
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
        apply(plugin = "io.gitlab.arturbosch.detekt")

        ktlint {
            debug.set(true)
            verbose.set(true)
            android.set(true)
            baseline.set(file("ktlint-baseline.xml"))
        }

        detekt {
            config.setFrom(rootProject.files("config/detekt/detekt.yml"))
            buildUponDefaultConfig = true
            baseline = file("detekt-baseline.xml")
        }

        group = rootProject.libs.versions.maven.artifactGroup.get()
        version = rootProject.libs.versions.maven.artifactVersion.get()

        afterEvaluate {
            configure<PublishingExtension> {
                publications {
                    register("release", MavenPublication::class) {
                        from(components["release"])
                    }
                }
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
