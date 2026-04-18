/**
 * File: OpenCoreClasspathTest.kt
 * Description: Ensures Open Core GenAI façade types resolve on the demo classpath without optional private modules.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   `./gradlew :app:testDebugUnitTest --tests com.fluvian.sdk.demo.OpenCoreClasspathTest`
 */
package com.fluvian.sdk.demo

import org.junit.Assert.assertNotNull
import org.junit.Test

class OpenCoreClasspathTest {

    @Test
    fun onDeviceGenAiProvider_class_loadable() {
        assertNotNull(Class.forName("com.fluvian.sdk.core.aicore.OnDeviceGenAiProvider"))
    }

    @Test
    fun onDeviceGenAiWarmup_class_loadable() {
        assertNotNull(Class.forName("com.fluvian.sdk.core.aicore.OnDeviceGenAiWarmup"))
    }

    @Test
    fun llmDecisionParser_class_loadable() {
        assertNotNull(Class.forName("com.fluvian.sdk.core.aicore.LlmDecisionParser"))
    }
}
