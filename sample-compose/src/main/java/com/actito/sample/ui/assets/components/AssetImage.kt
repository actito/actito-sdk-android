package com.actito.sample.ui.assets.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.rememberAsyncImagePainter
import com.actito.assets.models.ActitoAsset
import com.actito.sample.R

@Composable
fun AssetImage(
    asset: ActitoAsset,
    modifier: Modifier = Modifier,
) {
    val contentType = asset.metaData?.contentType

    val placeHolderPainter = when (contentType) {
        "image/jpeg", "image/gif", "image/png" -> {
            rememberAsyncImagePainter(model = asset.url)
        }

        "video/mp4" -> {
            painterResource(R.drawable.ic_baseline_video_camera_back_24)
        }

        "application/pdf" -> {
            painterResource(R.drawable.ic_baseline_picture_as_pdf_24)
        }

        "application/json" -> {
            painterResource(R.drawable.ic_baseline_data_object_24)
        }

        "text/javascript" -> {
            painterResource(R.drawable.ic_baseline_javascript_24)
        }

        "text/css" -> {
            painterResource(R.drawable.ic_baseline_css_24)
        }

        "text/html" -> {
            painterResource(R.drawable.ic_baseline_html_24)
        }

        null -> {
            painterResource(R.drawable.ic_baseline_text_fields_24)
        }

        else -> {
            throw Exception("Unknown asset content type")
        }
    }

    Image(
        modifier = modifier,
        painter = placeHolderPainter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}
