/**
 * File: publishing.gradle.kts
 * Description: **Deprecated script hook** — Maven `maven-publish` wiring for `:fluvian-sdk-core` now lives in
 * `fluvian-sdk-core/build.gradle.kts` (`afterEvaluate { configure<PublishingExtension> { … } }`) so the Kotlin DSL
 * resolves the `publishing` extension on the correct project.
 *
 * **Coordinates:** `com.fluvian.sdk:fluvian-sdk-core:1.3.6`
 *
 * **Private hosting properties** (optional, e.g. `~/.gradle/gradle.properties`):
 * - `fluvian.maven.releaseUrl`
 * - `fluvian.maven.username` / `fluvian.maven.password`
 *
 * **Local install:** `./gradlew :fluvian-sdk-core:publishFluvianSdkCorePublicationToMavenLocalRepository`
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 */

// Intentionally empty — see fluvian-sdk-core/build.gradle.kts.
