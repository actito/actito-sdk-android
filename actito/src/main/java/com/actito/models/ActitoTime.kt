package com.actito.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Represents time without a date or time zone.
 *
 * [ActitoTime] is used for time-based configurations such as
 * do-not-disturb windows. It stores hours and minutes in 24-hour
 * format and enforces valid ranges.
 *
 * @property hours Hour component of the time in 24-hour format. Must be between `0` and `23` (inclusive).
 * @property minutes Minute component of the time. Must be between `0` and `59` (inclusive).
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoTime(
    val hours: Int,
    val minutes: Int,
) : Parcelable {

    /**
     * Constructor for [ActitoTime] instance.
     *
     * Throws an [IllegalArgumentException] if [hours] or [minutes] are out of range.
     */
    init {
        if (hours !in 0..23 || minutes !in 0..59) {
            throw IllegalArgumentException("Invalid time '$hours:$minutes'.")
        }
    }

    /**
     * Creates an [ActitoTime] from a `HH:mm` formatted string.
     *
     * For example: `"09:30"` or `"18:05"`.
     *
     * Throws an [IllegalArgumentException] if the string is not in a valid format
     * or represents an invalid time.
     */
    public constructor(timeStr: String) : this(parse(timeStr).first, parse(timeStr).second)

    /**
     * Returns the time formatted as a `HH:mm` string.
     */
    public fun format(): String = String.format(Locale.US, "%02d:%02d", hours, minutes)

    public companion object {
        private fun parse(timeStr: String): Pair<Int, Int> {
            val parts = timeStr.split(":")
            if (parts.size != 2) throw IllegalArgumentException("Invalid time string.")

            val hours = parts[0].toIntOrNull()
            val minutes = parts[1].toIntOrNull()

            if (hours == null || minutes == null) throw IllegalArgumentException("Invalid time string.")

            return Pair(hours, minutes)
        }
    }
}
