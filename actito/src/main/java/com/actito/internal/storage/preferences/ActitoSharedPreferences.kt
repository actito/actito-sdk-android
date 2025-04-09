package com.actito.internal.storage.preferences

import android.content.Context
import androidx.core.content.edit
import com.actito.Actito
import com.actito.internal.logger
import com.actito.internal.moshi
import com.actito.internal.storage.preferences.entities.StoredDevice
import com.actito.models.ActitoApplication
import com.actito.models.ActitoEvent

internal class ActitoSharedPreferences(context: Context) {

    companion object {
        private const val PREFERENCES_FILE_NAME = "re.notifica.preferences"

        private const val PREFERENCE_MIGRATED = "re.notifica.preferences.migrated"
        private const val PREFERENCE_APPLICATION = "re.notifica.preferences.application"
        private const val PREFERENCE_DEVICE = "re.notifica.preferences.device"
        private const val PREFERENCE_PREFERRED_LANGUAGE = "re.notifica.preferences.preferred_language"
        private const val PREFERENCE_PREFERRED_REGION = "re.notifica.preferences.preferred_region"
        private const val PREFERENCE_CRASH_REPORT = "re.notifica.preferences.crash_report"
        private const val PREFERENCE_DEFERRED_LINK_CHECKED = "re.notifica.preferences.deferred_link_checked"
    }

    private val sharedPreferences = context.getSharedPreferences(
        PREFERENCES_FILE_NAME,
        Context.MODE_PRIVATE
    )

    var migrated: Boolean
        get() {
            return sharedPreferences.getBoolean(PREFERENCE_MIGRATED, false)
        }
        set(value) {
            sharedPreferences.edit {
                putBoolean(PREFERENCE_MIGRATED, value)
            }
        }

    var application: ActitoApplication?
        get() {
            return sharedPreferences.getString(PREFERENCE_APPLICATION, null)
                ?.let {
                    try {
                        Actito.moshi.adapter(ActitoApplication::class.java).fromJson(it)
                    } catch (e: Exception) {
                        logger.warning("Failed to decode the stored application.", e)

                        // Remove the corrupted device from local storage.
                        application = null

                        null
                    }
                }
        }
        set(value) {
            sharedPreferences.edit().also {
                if (value == null) it.remove(PREFERENCE_APPLICATION)
                else it.putString(
                    PREFERENCE_APPLICATION,
                    Actito.moshi.adapter(ActitoApplication::class.java).toJson(value)
                )
            }.apply()
        }

    var device: StoredDevice?
        get() {
            return sharedPreferences.getString(PREFERENCE_DEVICE, null)
                ?.let {
                    try {
                        Actito.moshi.adapter(StoredDevice::class.java).fromJson(it)
                    } catch (e: Exception) {
                        logger.warning("Failed to decode the stored device.", e)

                        // Remove the corrupted device from local storage.
                        device = null

                        null
                    }
                }
        }
        set(value) {
            sharedPreferences.edit {
                if (value == null) remove(PREFERENCE_DEVICE)
                else putString(
                    PREFERENCE_DEVICE,
                    Actito.moshi.adapter(StoredDevice::class.java).toJson(value)
                )
            }
        }

    var preferredLanguage: String?
        get() {
            return sharedPreferences.getString(
                PREFERENCE_PREFERRED_LANGUAGE,
                null
            )
        }
        set(value) {
            sharedPreferences.edit()
                .apply {
                    if (value == null) {
                        remove(PREFERENCE_PREFERRED_LANGUAGE)
                    } else {
                        putString(PREFERENCE_PREFERRED_LANGUAGE, value)
                    }
                }
                .apply()
        }

    var preferredRegion: String?
        get() {
            return sharedPreferences.getString(
                PREFERENCE_PREFERRED_REGION,
                null
            )
        }
        set(value) {
            sharedPreferences.edit()
                .apply {
                    if (value == null) {
                        remove(PREFERENCE_PREFERRED_REGION)
                    } else {
                        putString(PREFERENCE_PREFERRED_REGION, value)
                    }
                }
                .apply()
        }

    var crashReport: ActitoEvent?
        get() {
            return sharedPreferences.getString(PREFERENCE_CRASH_REPORT, null)
                ?.let {
                    try {
                        Actito.moshi.adapter(ActitoEvent::class.java).fromJson(it)
                    } catch (e: Exception) {
                        logger.warning("Failed to decode the stored crash report.", e)

                        // Remove the corrupted crash report from local storage.
                        crashReport = null

                        null
                    }
                }
        }
        set(value) {
            sharedPreferences.edit(commit = true) {
                if (value == null) remove(PREFERENCE_CRASH_REPORT)
                else putString(
                    PREFERENCE_CRASH_REPORT,
                    Actito.moshi.adapter(ActitoEvent::class.java).toJson(value)
                )
            }
        }

    var deferredLinkChecked: Boolean?
        get() {
            if (!sharedPreferences.contains(PREFERENCE_DEFERRED_LINK_CHECKED)) {
                return null
            }

            return sharedPreferences.getBoolean(PREFERENCE_DEFERRED_LINK_CHECKED, false)
        }
        set(value) = sharedPreferences.edit {
            if (value == null) {
                remove(PREFERENCE_DEFERRED_LINK_CHECKED)
            } else {
                putBoolean(PREFERENCE_DEFERRED_LINK_CHECKED, value)
            }
        }

    fun clear() {
        sharedPreferences.edit {
            clear()
        }
    }
}
