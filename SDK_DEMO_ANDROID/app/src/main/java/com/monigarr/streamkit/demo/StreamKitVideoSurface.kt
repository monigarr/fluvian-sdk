/**
 * File: StreamKitVideoSurface.kt
 * Description: TextureView bridge that forwards decoder surfaces to StreamKit without initializing ExoPlayer on the UI thread.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.2.0
 *
 * Usage:
 *   Host inside a Compose hierarchy; wire [onSurfaceChanged] to [com.monigarr.streamkit.core.StreamingClient.bindVideoSurface].
 *
 * Usage example:
 *   StreamKitVideoSurface(Modifier.fillMaxSize(), onSurfaceChanged = client::bindVideoSurface)
 */
package com.monigarr.streamkit.demo

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun StreamKitVideoSurface(
    modifier: Modifier = Modifier,
    onSurfaceChanged: (Surface?) -> Unit,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextureView(context).apply {
                surfaceTextureListener =
                    object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                            onSurfaceChanged(Surface(surface))
                        }

                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            onSurfaceChanged(null)
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
                    }
            }
        },
    )
}
