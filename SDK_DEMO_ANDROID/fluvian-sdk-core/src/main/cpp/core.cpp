/*
 * File: core.cpp
 * Description: JNI implementation backing NativeLib.stringFromJNI for the Fluvian SDK native sample library.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.6
 *
 * Usage:
 *   Built via CMake from fluvian-sdk-core; loaded with System.loadLibrary("core") from Kotlin.
 *
 * Usage example:
 *   NativeLib().stringFromJNI()
 */

#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_fluvian_sdk_core_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
