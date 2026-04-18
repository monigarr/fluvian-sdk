package com.fluvian.sdk.core.branding

/**
 * File: SdkBrandBundle.kt
 * Description: Optional white-label metadata carried with [com.fluvian.sdk.core.StreamConfig] for enterprise packaging.
 *
 * **ENTERPRISE SERVICES:** bespoke themes, legal footers, and OEM co-branding are delivered per contract outside this repo.
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Pass through [com.fluvian.sdk.core.StreamConfig.sdkBrand]; host shells read it for chrome / support links.
 *
 * Usage example:
 *   SdkBrandBundle(
 *       productDisplayName = "Acme Player",
 *       integratorSupportUrl = "https://support.acme.example",
 *       httpUserAgentProductName = "AcmeStreamKit",
 *       primaryColorArgb = 0xFF112233.toInt(),
 *   )
 */
data class SdkBrandBundle(
    val productDisplayName: String = "Fluvian SDK",
    val integratorSupportUrl: String? = null,
    val integratorLegalNotice: String? = null,
    /**
     * Short ASCII product token for Media3 default user-agent construction when [com.fluvian.sdk.core.StreamConfig.userAgent]
     * is null (passed to [androidx.media3.common.util.Util.getUserAgent]). Prefer no spaces.
     */
    val httpUserAgentProductName: String? = null,
    /** Optional primary brand color for host chrome (`0xAARRGGBB`). */
    val primaryColorArgb: Int? = null,
    /** Optional accent color for host chrome (`0xAARRGGBB`). */
    val accentColorArgb: Int? = null,
    /**
     * Optional non-secret tenant or SKU key so enterprise analytics sinks can route events without embedding secrets
     * in stream URLs or DRM headers.
     */
    val analyticsTenantKey: String? = null,
)
