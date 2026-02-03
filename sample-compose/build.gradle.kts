import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("linting")
    id("actito-application")
    alias(apps.plugins.kotlin.compose)
    alias(apps.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.actito.sample"
    compileSdk = apps.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.actito.sample.app"
        minSdk = apps.versions.android.minSdk.get().toInt()
        targetSdk = apps.versions.android.targetSdk.get().toInt()
        versionCode = 12
        versionName = "3.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(apps.androidx.appCompat)
    implementation(apps.androidx.core)
    implementation(apps.androidx.lifecycle.viewModel.compose)
    implementation(apps.androidx.activity.compose)
    implementation(platform(apps.androidx.compose.bom))
    implementation(apps.androidx.compose.ui)
    implementation(apps.androidx.compose.ui.graphics)
    implementation(apps.androidx.compose.ui.tooling.preview)
    implementation(apps.google.material)
    implementation(apps.androidx.compose.material3)
    debugImplementation(apps.androidx.compose.ui.tooling)
    implementation(apps.androidx.work.runtime)
    implementation(apps.androidx.datastore.preferences)

    // Moshi
    implementation(apps.moshi.kotlin)
    implementation(apps.moshi.adapters)
    ksp(apps.moshi.codegen)

    implementation(apps.bundles.coil)
    implementation(apps.bundles.nav3)

    implementation(apps.timber)

    implementation(project(":actito"))
    implementation(project(":actito-assets"))
    implementation(project(":actito-geo"))
    implementation(project(":actito-geo-beacons"))
    implementation(project(":actito-in-app-messaging"))
    implementation(project(":actito-inbox"))
    implementation(project(":actito-loyalty"))
    implementation(project(":actito-push"))
    implementation(project(":actito-push-ui"))
}
