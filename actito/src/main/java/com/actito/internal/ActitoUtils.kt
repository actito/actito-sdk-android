package com.actito.internal

import com.actito.InternalActitoApi

@InternalActitoApi
public object ActitoUtils {
    internal fun getEnabledPeerModules(): List<String> {
        return ActitoModule.Module.values()
            .filter { it.isPeer && it.isAvailable }
            .map { it.name.lowercase() }
    }
}
