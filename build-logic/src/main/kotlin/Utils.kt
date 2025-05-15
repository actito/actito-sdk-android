import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.util.Properties

internal fun String.isStableVersion(): Boolean = "^([0-9]+)\\.([0-9]+)\\.([0-9]+)\$".toRegex().matches(this)

internal fun loadProperties(file: File) = Properties().apply {
    file.inputStream().use {
        load(it)
    }
}

internal fun Project.versionCatalog(name: String): VersionCatalog =
    rootProject.extensions.getByType<VersionCatalogsExtension>().named(name)
