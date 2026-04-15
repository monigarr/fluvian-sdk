/**
 * File: StreamKitDefaultTimeBar.kt
 * Description: Media3 [DefaultTimeBar] in Compose; position/buffer from [StreamingDiagnostics], seeks via [StreamingClient.postPlayerOperation].
 * Author: monigarr@monigarr.com
 * Date: 2026-04-14
 * Version: 1.3.4
 *
 * Usage:
 *   Place below the video surface; keep [diagnostics] refreshed (e.g. [StreamingClient.refreshDiagnostics]) while playing.
 */
package com.monigarr.streamkit.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.TimeBar
import com.monigarr.streamkit.core.StreamingClient
import com.monigarr.streamkit.core.StreamingDiagnostics

@Composable
fun StreamKitDefaultTimeBar(
    modifier: Modifier = Modifier,
    client: StreamingClient,
    diagnostics: StreamingDiagnostics?,
    initialized: Boolean,
) {
    var scrubbing by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            DefaultTimeBar(context).apply {
                addListener(
                    object : TimeBar.OnScrubListener {
                        override fun onScrubStart(timeBar: TimeBar, positionMs: Long) {
                            scrubbing = true
                        }

                        override fun onScrubMove(timeBar: TimeBar, positionMs: Long) = Unit

                        override fun onScrubStop(timeBar: TimeBar, positionMs: Long, canceled: Boolean) {
                            scrubbing = false
                            if (!canceled) {
                                client.postPlayerOperation { player ->
                                    player.seekTo(positionMs)
                                }
                            }
                        }
                    },
                )
            }
        },
        update = { bar ->
            val d = diagnostics
            if (d == null || !initialized) {
                bar.setDuration(0L)
                bar.setPosition(0L)
                bar.setBufferedPosition(0L)
                bar.isEnabled = false
                return@AndroidView
            }
            val duration =
                if (d.durationMs == C.TIME_UNSET) C.TIME_UNSET else d.durationMs
            bar.setDuration(duration)
            if (!scrubbing) {
                bar.setPosition(d.currentPositionMs)
                bar.setBufferedPosition(d.bufferedPositionMs)
            }
            bar.isEnabled = d.isSeekable
        },
    )
}
