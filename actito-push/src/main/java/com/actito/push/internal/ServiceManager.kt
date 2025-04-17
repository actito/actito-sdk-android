package com.actito.push.internal

import com.actito.InternalActitoApi
import com.actito.internal.AbstractServiceManager
import com.actito.push.models.ActitoTransport

@InternalActitoApi
public abstract class ServiceManager : AbstractServiceManager() {

    public abstract val transport: ActitoTransport

    public abstract suspend fun getPushToken(): String

    internal companion object {
        private const val GMS_FQN = "com.actito.push.gms.internal.ServiceManager"

        internal fun create(): ServiceManager {
            return Factory.create(
                gms = GMS_FQN,
            )
        }
    }
}
