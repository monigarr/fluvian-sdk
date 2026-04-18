/**
 * File: build.gradle.kts
 * Description: Gradle build for the fluvian-sdk-core Android library (Kotlin, JNI, JaCoCo, kotlinx binary API dump).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.6
 *
 * Usage:
 *   Produces `jacocoFluvianSdkCoreDebug` XML/HTML used by Fluvian SDK CI coverage gates (Phase 8: LINE >= 15%; see docs/ARCHITECTURE.md §26).
 *
 * Usage example:
 *   ./gradlew :fluvian-sdk-core:apiCheck :fluvian-sdk-core:testDebugUnitTest :fluvian-sdk-core:jacocoFluvianSdkCoreDebug
 */
import org.gradle.api.file.FileCollection
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.binary.compatibility.validator)
    jacoco
    `maven-publish`
}

apiValidation {
    ignoredClasses.add("com.fluvian.sdk.core.BuildConfig")
}

android {
    namespace = "com.fluvian.sdk.core"
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
    publishing {
        singleVariant("release") {
            withSourcesJar()
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

tasks.register<JacocoReport>("jacocoFluvianSdkCoreDebug") {
    group = "verification"
    description = "JaCoCo XML/HTML for fluvian-sdk-core debug unit tests (Fluvian SDK CI)."
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
    // Bind strictly to the unit-test task output so Gradle 9+ does not treat the whole `build/` tree as an implicit input.
    executionData.setFrom(
        tasks.named<Test>("testDebugUnitTest").map { t ->
            t.extensions.getByType<JacocoTaskExtension>().destinationFile
        },
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoFluvianSdkCoreDebug/jacoco.xml"))
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/jacocoFluvianSdkCoreDebug/html"))
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
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

/**
 * Maven coordinates + optional private repository (see root [publishing.gradle.kts] for property names).
 */
afterEvaluate {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("fluvianSdkCore") {
                groupId = "com.fluvian.sdk"
                artifactId = "fluvian-sdk-core"
                version = "1.3.6"
                from(components["release"])
                pom {
                    name.set("Fluvian SDK Core")
                    description.set("Android playback and QoS core for the Fluvian streaming SDK.")
                    url.set("https://github.com/fluvian/fluvian-sdk")
                    licenses {
                        license {
                            name.set("LicenseRef-LICENSE")
                        }
                    }
                }
            }
        }
        repositories {
            val repoUrl = findProperty("fluvian.maven.releaseUrl")?.toString()?.trim().orEmpty()
            if (repoUrl.isNotEmpty()) {
                maven {
                    name = "fluvianPrivate"
                    url = uri(repoUrl)
                    val user = findProperty("fluvian.maven.username")?.toString()
                    val pass = findProperty("fluvian.maven.password")?.toString()
                    if (!user.isNullOrBlank() && pass != null) {
                        credentials {
                            username = user
                            password = pass
                        }
                    }
                }
            }
        }
    }
}
