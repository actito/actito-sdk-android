package com.actito.internal

internal object ActitoUtils {
    internal fun getEnabledModules(): List<String> =
        ActitoLaunchComponent.Module.entries
            .filter { it.isAvailable }
            .map { it.name.lowercase() }
}
