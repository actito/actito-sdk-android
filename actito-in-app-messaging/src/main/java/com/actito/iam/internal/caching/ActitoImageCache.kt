package com.actito.iam.internal.caching

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import com.actito.iam.models.ActitoInAppMessage
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object ActitoImageCache {
    private var portraitImage: Bitmap? = null
    private var landscapeImage: Bitmap? = null

    internal var isLoading: Boolean = false
        private set

    internal fun getOrientationConstrainedImage(context: Context): Bitmap? {
        return if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.landscapeImage ?: this.portraitImage
        } else {
            this.portraitImage ?: this.landscapeImage
        }
    }

    internal suspend fun preloadImages(
        context: Context,
        message: ActitoInAppMessage,
    ): Unit = withContext(Dispatchers.IO) {
        clear()

        try {
            isLoading = true

            if (!message.image.isNullOrBlank()) {
                portraitImage = loadImage(context, message.image.toUri())
            }

            if (!message.landscapeImage.isNullOrBlank()) {
                landscapeImage = loadImage(context, message.landscapeImage.toUri())
            }
        } finally {
            isLoading = false
        }
    }

    internal fun clear() {
        portraitImage = null
        landscapeImage = null
    }

    private suspend fun loadImage(
        context: Context,
        uri: Uri,
    ): Bitmap? = withContext(Dispatchers.IO) {
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .submit()
            .get()
    }
}
