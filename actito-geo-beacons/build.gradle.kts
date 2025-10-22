import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("actito-library")
    id("linting")
}

android {
    namespace = "com.actito.geo.beacons"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.android.buildTools.get()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
            explicitApi()
            optIn.add("com.actito.InternalActitoApi")
        }
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines)

    // Actito
    implementation(project(":actito"))
    implementation(project(":actito-geo"))
    implementation(project(":actito-utilities"))

    implementation(libs.androidx.core)

    // AltBeacon
    implementation(libs.altbeacon)
}
