dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
        create("apps") {
            from(files("../gradle/apps.versions.toml"))
        }
    }
}