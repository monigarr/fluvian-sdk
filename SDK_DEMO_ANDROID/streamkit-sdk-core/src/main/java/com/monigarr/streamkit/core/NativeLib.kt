package com.monigarr.streamkit.core

/**
 * File: NativeLib.kt
 * Description: JNI bridge to the streamkit-core native library for sample native integration.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.3.4
 *
 * Usage:
 *   Instantiate or reference NativeLib where JNI-backed helpers are required; ensure the
 *   native library "core" is packaged (see cpp/CMakeLists.txt).
 *
 * Usage example:
 *   val nativeLib = NativeLib()
 *   val message = nativeLib.stringFromJNI()
 */
class NativeLib {

    /**
     * A native method that is implemented by the 'core' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'core' library on application startup.
        init {
            System.loadLibrary("core")
        }
    }
}