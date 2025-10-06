package com.actito.assets.internal.network.push

import com.actito.Actito
import com.actito.assets.models.ActitoAsset
import com.actito.utilities.collections.filterNotNullRecursive
import com.actito.utilities.moshi.UseDefaultsWhenNull
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class FetchAssetsResponse(
    val assets: List<Asset>,
) {
    @UseDefaultsWhenNull
    @JsonClass(generateAdapter = true)
    internal data class Asset(
        val title: String,
        val description: String?,
        val key: String?,
        val url: String?,
        val button: Button?,
        val metaData: MetaData?,
        val extra: Map<String, Any?> = mapOf(),
    ) {

        internal fun toModel(): ActitoAsset {
            return ActitoAsset(
                title = title,
                description = description,
                key = key,
                url = key?.let { key ->
                    val host = Actito.servicesInfo?.hosts?.restApi ?: return@let null
                    "https://$host/asset/file/$key"
                },
                button = button?.let {
                    ActitoAsset.Button(
                        label = it.label,
                        action = it.action,
                    )
                },
                metaData = metaData?.let {
                    ActitoAsset.MetaData(
                        originalFileName = it.originalFileName,
                        contentType = it.contentType,
                        contentLength = it.contentLength,
                    )
                },
                extra = extra.filterNotNullRecursive { it.value },
            )
        }

        @JsonClass(generateAdapter = true)
        internal data class Button(
            val label: String?,
            val action: String?,
        )

        @JsonClass(generateAdapter = true)
        internal data class MetaData(
            val originalFileName: String,
            val contentType: String,
            val contentLength: Int,
        )
    }
}
