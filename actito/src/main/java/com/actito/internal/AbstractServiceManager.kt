package com.actito.internal

import com.actito.InternalActitoApi
import com.actito.utilities.logging.ActitoLogger

@InternalActitoApi
public abstract class AbstractServiceManager {

    public abstract val available: Boolean

    public object Factory {

        @PublishedApi
        internal val factoryLogger: ActitoLogger = logger

        public inline fun <reified T : AbstractServiceManager> create(gms: String): T {
            val implementation: T? = implementation(gms)

            if (implementation != null && implementation.available) {
                factoryLogger.debug("Detected GMS peer dependency. Setting it as the target platform.")
                return implementation
            }

            factoryLogger.warning(
                "No platform dependencies have been detected. Please include one of the platform-specific packages."
            )
            throw IllegalStateException(
                "No platform dependencies have been detected. Please include one of the platform-specific packages."
            )
        }

        @PublishedApi
        internal inline fun <reified T> implementation(fqn: String): T? {
            return try {
                val klass = Class.forName(fqn)
                klass.getConstructor().newInstance() as? T
            } catch (e: Exception) {
                null
            }
        }
    }
}
