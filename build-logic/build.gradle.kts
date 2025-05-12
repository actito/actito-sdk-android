plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://maven.notifica.re/releases")
}

dependencies {
    // Libraries
    implementation(libs.plugins.android.library.asDependency())
    implementation(libs.plugins.kotlin.android.asDependency())
    implementation(libs.plugins.google.ksp.asDependency())
    implementation(libs.plugins.ktlint.asDependency())
    implementation(libs.plugins.detekt.asDependency())
    implementation(libs.plugins.google.services.asDependency())

    // Apps
    implementation(apps.plugins.android.application.asDependency())
    implementation(apps.plugins.notificare.services.asDependency())
    implementation(apps.plugins.google.services.asDependency())
}

fun Provider<PluginDependency>.asDependency(): Provider<String> =
    this.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
