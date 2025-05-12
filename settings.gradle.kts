pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://maven.notifica.re/releases")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("apps") {
            from(files("gradle/apps.versions.toml"))
        }
    }
}

rootProject.name = "actito-sdk-android"

include(":actito")
include(":actito-assets")
include(":actito-geo")
include(":actito-geo-beacons")
include(":actito-in-app-messaging")
include(":actito-inbox")
include(":actito-loyalty")
include(":actito-push")
include(":actito-push-ui")
include(":actito-scannables")
include(":actito-user-inbox")
include(":actito-utilities")
include(":sample")
include(":sample-user-inbox")
includeBuild("build-logic")
