import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("actito-library")
    id("linting")
}

android {
    namespace = "com.actito.push.ui"
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

    buildFeatures {
        viewBinding = true
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
    implementation(project(":actito-utilities"))

    // Android
    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.core)
    implementation(libs.androidx.exifInterace)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.viewPager)

    implementation(libs.google.playServices.maps)
}
