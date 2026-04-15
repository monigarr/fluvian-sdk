/**
 * File: build.gradle.kts
 * Description: Gradle build for the streamkit-sdk-core Android library (Kotlin, JNI, JaCoCo, kotlinx binary API dump).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.4
 *
 * Usage:
 *   Produces `jacocoStreamkitCoreDebug` XML/HTML used by Echelon CI coverage gates (see docs/ARCHITECTURE.md §26).
 *
 * Usage example:
 *   ./gradlew :streamkit-sdk-core:apiCheck :streamkit-sdk-core:testDebugUnitTest :streamkit-sdk-core:jacocoStreamkitCoreDebug
 */
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.binary.compatibility.validator)
    jacoco
}

apiValidation {
    ignoredClasses.add("com.monigarr.streamkit.core.BuildConfig")
}

android {
    namespace = "com.monigarr.streamkit.core"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }
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
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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

tasks.register<JacocoReport>("jacocoStreamkitCoreDebug") {
    group = "verification"
    description = "JaCoCo XML/HTML for streamkit-sdk-core debug unit tests (Echelon CI)."
    dependsOn("testDebugUnitTest")

    val buildDirLayout = layout.buildDirectory.get().asFile
    val fileFilter = listOf(
        "**/R.class", "**/R\$*.class", "**/BuildConfig.*",
        "**/Manifest*.*", "**/*Test*.*", "**/DataBinder*", "**/BR.*"
    )

    // AGP / Kotlin plugin version differences place classes under different intermediates trees; include all that exist.
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
    // .exec may live under outputs/unit_test_code_coverage or nested task dirs depending on AGP.
    executionData.setFrom(
        fileTree(buildDirLayout) {
            include("**/*.exec")
        }
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoStreamkitCoreDebug/jacoco.xml"))
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/jacocoStreamkitCoreDebug/html"))
    }
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.ui)
    implementation(libs.google.mlkit.genai.common)
    implementation(libs.google.mlkit.genai.prompt)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
