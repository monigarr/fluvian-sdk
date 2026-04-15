/**
 * File: settings.gradle.kts
 * Description: Gradle settings for plugin management, repositories, and included modules (app, streamkit-sdk-core).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.1.0
 *
 * Usage:
 *   Consumed automatically by Gradle; defines `rootProject.name` and `include` for modules.
 *
 * Usage example:
 *   ./gradlew projects
 */
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LVSPOC StreamKit"
include(":app")
include(":streamkit-sdk-core")
