/**
 * File: build.gradle.kts
 * Description: Root Gradle build for the LVSPOC StreamKit Android multi-module project (plugin pins only).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.4
 *
 * Usage:
 *   Apply shared plugin versions to subprojects; open this directory in Android Studio and sync.
 *
 * Usage example:
 *   ./gradlew :streamkit-sdk-core:testDebugUnitTest
 */
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
}
