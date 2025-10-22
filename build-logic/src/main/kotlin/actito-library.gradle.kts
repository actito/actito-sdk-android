plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("com.vanniktech.maven.publish")
}

val libs = project.versionCatalog("libs")

project.group = libs.findVersion("maven.artifactGroup").get().toString()
project.version = libs.findVersion("maven.artifactVersion").get().toString()

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(project.group.toString(), project.name, project.version.toString())

    pom {
        name.set("Actito Android SDK")
        description.set("Actito is a Customer Engagement Platform made in Europe. This plugins makes it easy to integrate your native Android application with Actito.")
        url.set("https://github.com/actito/actito-sdk-android/")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/license/mit")
                distribution.set("https://opensource.org/license/mit")
            }
        }

        developers {
            developer {
                id.set("Actito")
                name.set("Actito")
                url.set("https://github.com/actito/")
            }
        }

        scm {
            url.set("https://github.com/actito/actito-sdk-android/")
            connection.set("scm:git:git://github.com/actito/actito-sdk-android.git")
            developerConnection.set("scm:git:ssh://git@github.com/actito/actito-sdk-android.git")
        }
    }
}
