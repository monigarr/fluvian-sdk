/**
 * File: publishing.gradle.kts
 * Description: Maven publication sketch for StreamKit artifacts (groupId, artifactId, version pin).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.4
 *
 * Usage:
 *   Apply from the root build when wiring `maven-publish` to the streamkit-sdk-core release component.
 *
 * Usage example:
 *   // in root build.gradle.kts: apply(from = "publishing.gradle.kts")
 */
plugins {
    `maven-publish`
}

// The SDK is designed for Maven distribution with strict
// dependency isolation to avoid conflicts with host apps.
publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["release"])
            groupId = "com.monigarr.streamkit"
            artifactId = "streamkit-sdk-core"
            version = "1.3.4"
        }
    }
}
