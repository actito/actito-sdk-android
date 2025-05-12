plugins {
    id("libraries")
    id("publish")
    id("linting")
}

group = rootProject.libs.versions.maven.artifactGroup.get()
version = rootProject.libs.versions.maven.artifactVersion.get()

android {
    namespace = "com.actito.geo"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.android.buildTools.get()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        consumerProguardFiles("consumer-rules.pro")
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs = listOf(
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
    implementation(libs.kotlinx.coroutines.playServices)

    // Actito
    implementation(project(":actito"))
    implementation(project(":actito-utilities"))

    // Android
    implementation(libs.androidx.core)

    // Google Play Services
    implementation(libs.google.playServices.location)

    // OkHttp
    implementation(libs.okhttp)

    // Moshi
    implementation(libs.bundles.moshi)
    ksp(libs.moshi.codegen)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}
