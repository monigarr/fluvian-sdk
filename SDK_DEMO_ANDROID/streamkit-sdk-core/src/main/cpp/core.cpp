/*
 * File: core.cpp
 * Description: JNI implementation backing NativeLib.stringFromJNI for the streamkit-core library.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.4
 *
 * Usage:
 *   Built via CMake from streamkit-sdk-core; loaded with System.loadLibrary("core") from Kotlin.
 *
 * Usage example:
 *   NativeLib().stringFromJNI()
 */

#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_monigarr_streamkit_core_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
