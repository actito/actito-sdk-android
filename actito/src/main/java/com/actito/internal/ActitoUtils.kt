package com.actito.internal

import com.actito.InternalActitoApi

@InternalActitoApi
public object ActitoUtils {
    internal fun getEnabledPeerModules(): List<String> =
        ActitoLaunchComponent.Module.entries
            .filter { it.isPeer && it.isAvailable }
            .map { it.name.lowercase() }
}
