package com.monigarr.streamkit.core

/**
 * File: DrmConfig.kt
 * Description: Widevine-oriented DRM bootstrap: license endpoint and optional request headers (tokens).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.1.0
 *
 * Usage:
 *   Pass into [StreamingClientImpl] when constructing the client; applied to [androidx.media3.common.MediaItem] DRM.
 *
 * Usage example:
 *   val drm = DrmConfig(licenseUrl = "https://license.example.com", headers = mapOf("Authorization" to "Bearer …"))
 */
data class DrmConfig(
    val licenseUrl: String,
    val headers: Map<String, String> = emptyMap(),
)
