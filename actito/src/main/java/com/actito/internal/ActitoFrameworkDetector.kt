package com.actito.internal

import android.content.Context
import java.util.zip.ZipFile

internal object ActitoFrameworkDetector {
    internal fun detect(context: Context): FrameworkInfo? {
        return when {
            isFlutter() -> FrameworkInfo("Flutter", null)
            isExpo() -> FrameworkInfo("Expo (React Native)", getReactNativeVersion())
            isReactNative() -> FrameworkInfo("React Native", getReactNativeVersion())
            isCapacitor() -> FrameworkInfo("Capacitor", null)
            isCordova() -> FrameworkInfo("Cordova", getCordovaVersion(context))
            isDotNETMAUI(context) -> FrameworkInfo(".NET MAUI", null)
            else -> null
        }
    }

    private fun isFlutter(): Boolean = classExists("io.flutter.embedding.engine.FlutterEngine")

    private fun isExpo(): Boolean = classExists("expo.modules.ApplicationLifecycleDispatcher")

    private fun isReactNative(): Boolean = classExists("com.facebook.react.bridge.ReactContext")

    private fun isCordova(): Boolean = classExists("org.apache.cordova.CordovaActivity")

    private fun isCapacitor(): Boolean = classExists("com.getcapacitor.BridgeActivity")

    private fun isDotNETMAUI(context: Context): Boolean {
        return try {
            val apkPath = context.applicationInfo.sourceDir

            ZipFile(apkPath).use { zip ->
                zip.entries().asSequence().any {
                    it.name.contains("libmonosgen")
                }
            }
        } catch (_: Throwable) {
            false
        }
    }

    private fun getReactNativeVersion(): String? {
        return try {
            val clazz = Class.forName("com.facebook.react.modules.systeminfo.ReactNativeVersion")

            @Suppress("UNCHECKED_CAST")
            val versionMap = clazz.getField("VERSION").get(null) as? Map<String, Any>
                ?: throw IllegalArgumentException("Version field not found")

            val major = requireNotNull(versionMap["major"])
            val minor = requireNotNull(versionMap["minor"])
            val patch = requireNotNull(versionMap["patch"])

            "$major.$minor.$patch"
        } catch (_: Throwable) {
            null
        }
    }

    private fun getCordovaVersion(context: Context): String? {
        return try {
            val cordovaJs = context.assets
                .open("www/cordova.js")
                .bufferedReader()
                .use { it.readText() }

            val regex = Regex("PLATFORM_VERSION_BUILD_LABEL\\s*=\\s*'([^']+)'")

            regex.find(cordovaJs)
                ?.groupValues
                ?.getOrNull(1)
        } catch (_: Throwable) {
            null
        }
    }

    private fun classExists(name: String): Boolean {
        return try {
            Class.forName(name)
            true
        } catch (_: Throwable) {
            false
        }
    }

    internal data class FrameworkInfo(
        val name: String,
        val version: String?,
    )
}
