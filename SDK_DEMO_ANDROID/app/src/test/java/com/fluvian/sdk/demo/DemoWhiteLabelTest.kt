package com.fluvian.sdk.demo

import com.fluvian.sdk.core.aicore.AIConfig
import com.fluvian.sdk.core.aicore.AIProviderType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * File: DemoWhiteLabelTest.kt
 * Description: Locks demo white-label tokens and resolver routing for regression safety.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 */
class DemoWhiteLabelTest {

    @Test
    fun demoSdkBrandBundle_exposesUaTokenAndTenant() {
        val b = demoSdkBrandBundle()
        assertEquals("FluvianDemoShell", b.httpUserAgentProductName)
        assertEquals("demo-tenant-open-core", b.analyticsTenantKey)
        assertTrue(b.productDisplayName.contains("white-label", ignoreCase = true))
    }

    @Test
    fun demoAiProviderResolver_returnsNullForNonOnDevice() {
        val r = demoAiProviderResolver()
        val cfg = AIConfig(AIProviderType.LOCAL, modelName = "rules-v1")
        assertNull(r.resolve(cfg, applicationContext = null))
    }

    @Test
    fun demoAiProviderResolver_openCoreDelegatesOnDeviceToFactory() {
        val r = demoAiProviderResolver()
        val cfg = AIConfig(AIProviderType.ON_DEVICE_GENAI, modelName = "nano")
        assertNull(r.resolve(cfg, applicationContext = null))
    }
}
