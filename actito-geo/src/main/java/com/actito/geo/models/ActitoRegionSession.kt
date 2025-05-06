package com.actito.geo.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoRegionSession(
    val regionId: String,
    val start: Date,
    val end: Date?,
    val locations: MutableList<ActitoLocation>,
) : Parcelable {

    public companion object {
        public operator fun invoke(region: ActitoRegion): ActitoRegionSession {
            return ActitoRegionSession(
                regionId = region.id,
                start = Date(),
                end = null,
                locations = mutableListOf(),
            )
        }
    }
}
