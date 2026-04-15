package com.monigarr.streamkit.core

/**
 * File: EchelonProgramInfo.kt
 * Description: Immutable public metadata for documentation and CI alignment (SemVer with PRD/README).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.4
 *
 * Usage:
 *   Read constants from host apps or tests to assert release alignment; do not use for security decisions.
 *
 * Usage example:
 *   val v = EchelonProgramInfo.DOCUMENT_VERSION
 *
 * Release alignment: the reference `:app` module sets `defaultConfig.versionName` to the same SemVer as [DOCUMENT_VERSION].
 * The demo badge reads `BuildConfig.VERSION_NAME`; `:app:testDebugUnitTest` fails if it diverges from [DOCUMENT_VERSION]
 * (`ExampleUnitTest.demoVersionName_matchesEchelonDocumentVersion`).
 */
object EchelonProgramInfo {
    const val DOCUMENT_VERSION: String = "1.3.4"
    const val PROGRAM_NAME: String = "LVSPOC StreamKit"

    /** Resolvable at runtime for tests, diagnostics, and JaCoCo line coverage (not inlined like const alone). */
    fun describe(): String = "$PROGRAM_NAME $DOCUMENT_VERSION"
}
