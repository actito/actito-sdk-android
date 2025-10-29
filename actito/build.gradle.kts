import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("actito-library")
    id("linting")
}

android {
    namespace = "com.actito"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.android.buildTools.get()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        consumerProguardFiles("consumer-rules.pro")

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
        }
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
    implementation(project(":actito-utilities"))

    // Android
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core)
    implementation(libs.androidx.work.runtime)
    implementation(libs.google.androidInstallReferrer)

    // Android: Room
    implementation(libs.bundles.androidx.room)
    ksp(libs.androidx.room.compiler)

    // OkHttp
    implementation(libs.bundles.okhttp)

    // Moshi
    implementation(libs.bundles.moshi)
    ksp(libs.moshi.codegen)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}
