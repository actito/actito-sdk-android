plugins {
    `maven-publish`
}

val properties = loadProperties(rootProject.file("local.properties"))

val artifactChannel = if (version.toString().isStableVersion()) "releases" else "prereleases"

afterEvaluate {
    publishing {
        publications {
            register("release", MavenPublication::class) {
                from(components["release"])
            }
        }
    }
}
