import java.io.File
import java.util.Properties

internal fun String.isStableVersion(): Boolean = "^([0-9]+)\\.([0-9]+)\\.([0-9]+)\$".toRegex().matches(this)

internal fun loadProperties(file: File) = Properties().apply {
    file.inputStream().use {
        load(it)
    }
}