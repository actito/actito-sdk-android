plugins {
    id("actito-library")
    id("publish")
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
                "proguard-rules.pro"
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

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs += listOf(
            "-Xexplicit-api=strict",
            "-opt-in=com.actito.InternalActitoApi",
        )
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
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
