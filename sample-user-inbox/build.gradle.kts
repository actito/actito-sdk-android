import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    id("linting")
    id("actito-application")
}

val properties = loadProperties("local.properties")

android {
    namespace = "com.actito.sample.user.inbox"
    compileSdk = apps.versions.android.compileSdk.get().toInt()
    buildToolsVersion = apps.versions.android.buildTools.get()

    defaultConfig {
        applicationId = "com.actito.sample.user.inbox.app"
        minSdk = apps.versions.android.minSdk.get().toInt()
        targetSdk = apps.versions.android.targetSdk.get().toInt()
        versionCode = 12
        versionName = "3.0.0"

        manifestPlaceholders["googleMapsApiKey"] = properties.getProperty("google.maps.key")
        manifestPlaceholders["auth0Domain"] = properties.getProperty("user.inbox.login.domain")
        manifestPlaceholders["auth0Scheme"] = "auth.re.notifica.sample.user.inbox.app.dev"

        resValue("string", "user_inbox_base_url", properties.getProperty("user.inbox.base.url"))
        resValue("string", "user_inbox_fetch_inbox_url", properties.getProperty("user.inbox.fetch.inbox.url"))
        resValue("string", "user_inbox_register_device_url", properties.getProperty("user.inbox.register.device.url"))
        resValue("string", "user_inbox_login_domain", properties.getProperty("user.inbox.login.domain"))
        resValue("string", "user_inbox_login_client_id", properties.getProperty("user.inbox.login.client.id"))
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
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

    // Retrofit
    implementation(apps.bundles.retrofit)

    // Auth0
    implementation(apps.auth0)

    implementation(project(":actito"))
    implementation(project(":actito-push"))
    implementation(project(":actito-push-ui"))
    implementation(project(":actito-user-inbox"))
}
