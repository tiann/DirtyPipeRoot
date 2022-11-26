// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("dirtypipecheck");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("dirtypipecheck")
//      }
//    }

#include <jni.h>
#include <string>
#include "explot.h"

extern "C"
JNIEXPORT jboolean JNICALL
Java_me_weishu_dirtypipecheck_Check_check(JNIEnv *env, jclass clazz, jstring path) {

    auto cpath = env->GetStringUTFChars(path, nullptr);

    exploit(cpath, 1, "test");

    env->ReleaseStringUTFChars(path, cpath);
    return JNI_TRUE;
}