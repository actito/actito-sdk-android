plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

val libs = project.versionCatalog("libs")

project.group = libs.findVersion("maven.artifactGroup").get().toString()
project.version = libs.findVersion("maven.artifactVersion").get().toString()
