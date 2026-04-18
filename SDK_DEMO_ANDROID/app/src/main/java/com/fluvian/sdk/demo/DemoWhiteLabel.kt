/**
 * File: DemoWhiteLabel.kt
 * Description: Reference white-label [com.fluvian.sdk.core.branding.SdkBrandBundle] plus a sample [com.fluvian.sdk.core.aicore.AIProviderResolver]
 * for the Fluvian demo app (shows enterprise wiring without forking core).
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 */
package com.fluvian.sdk.demo

import com.fluvian.sdk.core.aicore.AIProviderResolver
import com.fluvian.sdk.core.branding.SdkBrandBundle

private fun argb(
    a: Int,
    r: Int,
    g: Int,
    b: Int,
): Int = (a shl 24) or (r shl 16) or (g shl 8) or b

/** Brand tokens aligned with [com.fluvian.sdk.demo.ui.theme] for host chrome and analytics fan-out demos. */
fun demoSdkBrandBundle(): SdkBrandBundle =
    SdkBrandBundle(
        productDisplayName = "Fluvian SDK Demo (white-label)",
        integratorSupportUrl = "https://monigarr.com",
        integratorLegalNotice = "Reference application — not a production content or DRM SLA.",
        httpUserAgentProductName = "FluvianDemoShell",
        primaryColorArgb = argb(0xFF, 0xC4, 0xB5, 0xFD),
        accentColorArgb = argb(0xFF, 0x81, 0xC7, 0x84),
        analyticsTenantKey = "demo-tenant-open-core",
    )

/**
 * Sample resolver hook: return **null** so [com.fluvian.sdk.core.aicore.AIProviderFactory] continues to own routing in Open Core.
 * Enterprises inject a private [com.fluvian.sdk.core.aicore.AIProvider] here under NDA artifacts while keeping the same
 * [com.fluvian.sdk.core.StreamConfig] surface.
 */
fun demoAiProviderResolver(): AIProviderResolver =
    AIProviderResolver { _, _ ->
        null
    }
