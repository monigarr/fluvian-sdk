package com.fluvian.sdk.core

/**
 * File: DrmConfig.kt
 * Description: Widevine-oriented DRM bootstrap: **integrator-supplied** license endpoint and optional request headers only.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.3.6
 *
 * **Public repository safety:** This type defines configuration *shape* only. Do not paste customer license URLs,
 * vendor-specific proxy formats, bearer tokens, or opaque license secrets into source control, public issues, or
 * committed samples. Supply values at runtime from your backend or secure host storage ([com.fluvian.sdk.core.security.FluvianSecretStore] patterns).
 *
 * Usage:
 *   Pass into [com.fluvian.sdk.core.player.StreamingClientImpl] when constructing the client; applied to
 *   [androidx.media3.common.MediaItem] DRM.
 *
 * Usage example (placeholders only — use your CAO / multi-DRM vendor documentation privately for real wiring):
 *   val drm = DrmConfig(
 *       licenseUrl = "https://license-service.example.invalid/widevine",
 *       headers = mapOf("Authorization" to "Bearer <short-lived-token-from-your-backend>"),
 *   )
 *
 * **Operational note:** Optional [drm_token] and [drm_ssm] exist only for integrator proxy or session flows your DRM
 * vendor documents under NDA. This public tree intentionally omits real vendor hosts, token grammars, and proxy URL
 * patterns so nothing in documentation can be copied into a working production stack without your own contracts and keys.
 */
data class DrmConfig(
    /** Integrator-owned license acquisition URL; never commit a customer-specific or production host in public source. */
    val licenseUrl: String,
    /** HTTP headers for the license request (e.g. temporary bearer); values must be minted server-side, not hard-coded. */
    val headers: Map<String, String> = emptyMap(),
    /** Reserved for vendor-documented proxy or session fields; keep empty unless your DRM integration requires it. */
    val drm_token: String = "",
    /** Reserved for vendor-documented session / SSM material; keep empty unless your DRM integration requires it. */
    val drm_ssm: String = "",
)
