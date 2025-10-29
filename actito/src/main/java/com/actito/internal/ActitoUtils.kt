package com.actito.internal

internal object ActitoUtils {
    internal fun getEnabledPeerModules(): List<String> =
        ActitoLaunchComponent.Module.entries
            .filter { it.isPeer && it.isAvailable }
            .map { it.name.lowercase() }
}
