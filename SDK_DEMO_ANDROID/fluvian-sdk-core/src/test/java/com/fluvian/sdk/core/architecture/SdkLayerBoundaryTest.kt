// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

/**
 * File: SdkLayerBoundaryTest.kt
 * Description: Lightweight source-level checks so `aicore`, `interpret`, and `qos` dependency rules stay explicit.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   `./gradlew :fluvian-sdk-core:testDebugUnitTest --tests com.fluvian.sdk.core.architecture.SdkLayerBoundaryTest`
 *
 * Usage example:
 *   CI runs full `:fluvian-sdk-core:testDebugUnitTest`.
 */
package com.fluvian.sdk.core.architecture

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SdkLayerBoundaryTest {

    private val moduleJavaRoot: File
        get() {
            val cwd = File(System.getProperty("user.dir") ?: ".")
            val candidates =
                listOf(
                    cwd,
                    File(cwd, "fluvian-sdk-core"),
                    File(cwd, "SDK_DEMO_ANDROID/fluvian-sdk-core"),
                )
            return candidates.firstOrNull { File(it, "src/main/java/com/fluvian/sdk/core").isDirectory }
                ?: error(
                    "Could not resolve fluvian-sdk-core module root from user.dir=${cwd.absolutePath}; tried " +
                        candidates.joinToString { it.absolutePath },
                )
        }

    @Test
    fun aicore_sources_do_not_import_qos_package() {
        val aicore = File(moduleJavaRoot, "src/main/java/com/fluvian/sdk/core/aicore")
        assertTrue("aicore dir missing under ${moduleJavaRoot.absolutePath}", aicore.isDirectory)
        val violations = mutableListOf<String>()
        aicore.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                file.readLines().forEachIndexed { idx, line ->
                    val t = line.trim()
                    if (t.startsWith("import com.fluvian.sdk.core.qos.")) {
                        violations += "${file.name}:${idx + 1}: $t"
                    }
                }
            }
        assertTrue("aicore must not depend on qos:\n${violations.joinToString("\n")}", violations.isEmpty())
    }

    @Test
    fun interpret_sources_do_not_import_qos_or_aicore() {
        val interpret = File(moduleJavaRoot, "src/main/java/com/fluvian/sdk/core/interpret")
        assertTrue("interpret dir missing under ${moduleJavaRoot.absolutePath}", interpret.isDirectory)
        val violations = mutableListOf<String>()
        interpret.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                file.readLines().forEachIndexed { idx, line ->
                    val t = line.trim()
                    if (t.startsWith("import com.fluvian.sdk.core.qos.") ||
                        t.startsWith("import com.fluvian.sdk.core.aicore.")
                    ) {
                        violations += "${file.name}:${idx + 1}: $t"
                    }
                }
            }
        assertTrue("interpret must stay independent:\n${violations.joinToString("\n")}", violations.isEmpty())
    }

    @Test
    fun qos_sources_only_import_aicore_for_facade_types() {
        val qos = File(moduleJavaRoot, "src/main/java/com/fluvian/sdk/core/qos")
        assertTrue("qos dir missing under ${moduleJavaRoot.absolutePath}", qos.isDirectory)
        val allowedAicoreImports =
            setOf(
                "import com.fluvian.sdk.core.aicore.QoSData",
                "import com.fluvian.sdk.core.aicore.OptimizationDecision",
            )
        val violations = mutableListOf<String>()
        qos.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                file.readLines().forEachIndexed { idx, line ->
                    val t = line.trim()
                    if (t.startsWith("import com.fluvian.sdk.core.aicore.") && t !in allowedAicoreImports) {
                        violations += "${file.name}:${idx + 1}: $t"
                    }
                }
            }
        assertTrue("qos may only import QoSData/OptimizationDecision from aicore:\n${violations.joinToString("\n")}", violations.isEmpty())
    }
}
