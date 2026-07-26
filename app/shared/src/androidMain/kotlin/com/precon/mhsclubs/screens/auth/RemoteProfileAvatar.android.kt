package com.precon.mhsclubs.screens.auth

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
actual fun RemoteProfileAvatar(
    avatarUrl: String,
    displayName: String,
    modifier: Modifier
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = avatarUrl) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                (URL(avatarUrl).openConnection() as HttpURLConnection).run {
                    connectTimeout = 10_000
                    readTimeout = 10_000
                    inputStream.use(BitmapFactory::decodeStream)
                }
            }.getOrNull()
        }
    }

    bitmap?.let { image ->
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = "$displayName profile picture",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape)
        )
    } ?: Icon(Icons.Default.AccountCircle, contentDescription = "$displayName profile picture", modifier = modifier)
}
