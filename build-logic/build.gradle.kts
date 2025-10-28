plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://maven.notifica.re/releases")
    mavenLocal()
}

dependencies {
    // Libraries
    implementation(libs.plugins.android.library.asDependency())
    implementation(libs.plugins.kotlin.android.asDependency())
    implementation(libs.plugins.google.ksp.asDependency())
    implementation(libs.plugins.ktlint.asDependency())
    implementation(libs.plugins.detekt.asDependency())
    implementation(libs.plugins.mavenPublish.asDependency())

    // Apps
    implementation(apps.plugins.android.application.asDependency())
    implementation(apps.plugins.actito.services.asDependency())
    implementation(apps.plugins.google.services.asDependency())
}

private fun Provider<PluginDependency>.asDependency(): Provider<String> =
    this.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
