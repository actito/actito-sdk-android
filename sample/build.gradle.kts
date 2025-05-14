import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    id("linting")
    id("apps")
}

val properties = loadProperties("local.properties")

android {
    namespace = "com.actito.sample"
    compileSdk = apps.versions.android.compileSdk.get().toInt()
    buildToolsVersion = apps.versions.android.buildTools.get()

    defaultConfig {
        applicationId = "com.actito.sample.app"
        minSdk = apps.versions.android.minSdk.get().toInt()
        targetSdk = apps.versions.android.targetSdk.get().toInt()
        versionCode = 12
        versionName = "3.0.0"

        manifestPlaceholders["googleMapsApiKey"] = properties.getProperty("google.maps.key")

        resValue("string", "sample_user_id", properties.getProperty("userId"))
        resValue("string", "sample_user_name", properties.getProperty("userName"))
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = properties.getProperty("keystore.debug.store.password")
            keyAlias = properties.getProperty("keystore.debug.key.alias")
            keyPassword = properties.getProperty("keystore.debug.key.password")
        }
        create("release") {
            storeFile = file("release.keystore")
            storePassword = properties.getProperty("keystore.release.store.password")
            keyAlias = properties.getProperty("keystore.release.key.alias")
            keyPassword = properties.getProperty("keystore.release.key.password")
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".dev"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions += "api"
    productFlavors {
        create("apiTest") {
            dimension = "api"
            applicationId = "com.actito.sample.app.test"
        }
        create("apiProduction") {
            dimension = "api"
        }
    }

    applicationVariants.configureEach {
        when (name) {
            "apiTestDebug" -> {
                @Suppress("ktlint:standard:argument-list-wrapping")
                resValue("string", "notificare_app_links_hostname", "\"618d0f4edc09fbed1864e8d0.applinks-test.notifica.re\"")
                resValue("string", "notificare_dynamic_link_hostname", "\"sample-app-dev.test.ntc.re\"")
            }
            "apiTestRelease" -> {
                @Suppress("ktlint:standard:argument-list-wrapping")
                resValue("string", "notificare_app_links_hostname", "\"654d017fc468efc19379921e.applinks-test.notifica.re\"")
                resValue("string", "notificare_dynamic_link_hostname", "\"sample-app.test.ntc.re\"")
            }
            "apiProductionDebug" -> {
                resValue("string", "notificare_app_links_hostname", "\"61644511218adebf72c5449b.applinks.notifica.re\"")
                resValue("string", "notificare_dynamic_link_hostname", "\"sample-app-dev.ntc.re\"")
            }
            "apiProductionRelease" -> {
                resValue("string", "notificare_app_links_hostname", "\"6511625f445cc1c81d47fd6f.applinks.notifica.re\"")
                resValue("string", "notificare_dynamic_link_hostname", "\"sample-app.ntc.re\"")
            }
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    implementation(apps.kotlinx.coroutines)

    implementation(apps.androidx.appCompat)
    implementation(apps.androidx.constraintLayout)
    implementation(apps.androidx.core)
    implementation(apps.androidx.datastore.preferences)
    implementation(apps.androidx.fragment)
    implementation(apps.bundles.androidx.lifecycle)
    implementation(apps.bundles.androidx.navigation)
    implementation(apps.androidx.work.runtime)

    implementation(apps.google.material)
    implementation(apps.timber)

    // Glide
    implementation(apps.glide)
    ksp(apps.glide.ksp)

    // Moshi
    implementation(apps.moshi.kotlin)
    implementation(apps.moshi.adapters)
    ksp(apps.moshi.codegen)

    implementation(project(":actito"))
    implementation(project(":actito-assets"))
    implementation(project(":actito-geo"))
    implementation(project(":actito-geo-beacons"))
    implementation(project(":actito-in-app-messaging"))
    implementation(project(":actito-inbox"))
    implementation(project(":actito-loyalty"))
    implementation(project(":actito-push"))
    implementation(project(":actito-push-ui"))
    implementation(project(":actito-scannables"))
}
