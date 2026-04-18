/**
 * File: settings.gradle.kts
 * Description: Gradle settings for plugin management, repositories, and included modules (app, fluvian-sdk-core).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.6
 *
 * Usage:
 *   Consumed automatically by Gradle; defines `rootProject.name` and `include` for app and Open Core (`:fluvian-sdk-core`).
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

rootProject.name = "Fluvian SDK"
include(":app")
include(":fluvian-sdk-core")
