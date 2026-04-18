/**
 * File: build.gradle.kts
 * Description: Gradle build for the Fluvian SDK demo Android application (Compose, Material3).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Declares dependencies on AndroidX, Compose BOM, and the local fluvian-sdk-core library module.
 *
 * Usage example:
 *   ./gradlew :app:assembleDebug
 */
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    jacoco
}

android {
    namespace = "com.fluvian.sdk.demo"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.fluvian.sdk.demo"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "1.3.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    jvmToolchain(11)
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoAppDebug") {
    group = "verification"
    description = "JaCoCo XML/HTML for app debug unit tests."
    dependsOn("testDebugUnitTest")

    val buildDirLayout = layout.buildDirectory.get().asFile
    val fileFilter = listOf(
        "**/R.class", "**/R\$*.class", "**/BuildConfig.*",
        "**/Manifest*.*", "**/*Test*.*", "**/DataBinder*", "**/BR.*"
    )

    val classDirPaths = listOf(
        "${buildDirLayout}/intermediates/javac/debug/compileDebugJavaWithJavac/classes",
        "${buildDirLayout}/intermediates/classes/debug",
        "${buildDirLayout}/tmp/kotlin-classes/debug",
        "${buildDirLayout}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes",
    )
    val classTrees: List<FileCollection> =
        classDirPaths.map { p ->
            fileTree(p) { exclude(fileFilter) }
        }
    classDirectories.setFrom(files(classTrees))
    sourceDirectories.setFrom(
        files(
            "$projectDir/src/main/java",
            "$projectDir/src/main/kotlin"
        )
    )
    executionData.setFrom(
        tasks.named<Test>("testDebugUnitTest").map { t ->
            t.extensions.getByType<JacocoTaskExtension>().destinationFile
        },
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoAppDebug/jacoco.xml"))
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/jacocoAppDebug/html"))
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.ui.compose.material3)
    implementation(project(":fluvian-sdk-core"))
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
