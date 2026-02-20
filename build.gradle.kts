// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.util.Properties

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    val localFile = rootProject.file("local.properties")

    if (localFile.exists()) {
        val localProperties = Properties()
        localProperties.load(localFile.inputStream())

        tasks.withType<Test>().configureEach {
            systemProperty("applicationKey", localProperties.getProperty("applicationKey"))
            systemProperty("applicationSecret", localProperties.getProperty("applicationSecret"))
            systemProperty("restApi", localProperties.getProperty("restApi"))
            systemProperty("shortLinks", localProperties.getProperty("shortLinks"))
            systemProperty("appLinks", localProperties.getProperty("appLinks"))
        }
    }
}
