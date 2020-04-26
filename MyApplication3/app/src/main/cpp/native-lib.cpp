#include <jni.h>
#include <string>
#include <android/log.h>

#include <regex>
#include <sstream>
#include <string>
#include <vector>

#include <sys/wait.h>
#include <unistd.h>
#include <dlfcn.h>

#include "inlineHook.h"

int (*old_puts)(const char *) = NULL;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_myapplication_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    __android_log_print(ANDROID_LOG_INFO, "xiabo", "my name is %s\n", hello.c_str());


    FILE* maps = fopen("/sdcard/maps",  "w+");
    FILE* file = fopen("/proc/self/maps",  "r");

    __android_log_print(ANDROID_LOG_INFO, "xiabo", "fopen addr %p\n", fopen);
    Dl_info info;
    if (dladdr((void*) fopen, &info)) {
        __android_log_print(ANDROID_LOG_INFO, "xiabo", "so %s, base %p, fopen addr %p\n", info.dli_fname, info.dli_fbase, (void*) fopen);
    }

    char buff[1024];
    if (file != nullptr) {
        __android_log_print(ANDROID_LOG_INFO, "xiabo", "++++++++++++++++++++++++++++");
        while ((fgets(buff, 1024, file)) != NULL) {
            __android_log_print(ANDROID_LOG_INFO, "xiabo", " %s", buff);
            if (maps != nullptr) {
                fputs(buff,maps);
            }
        }
        if (maps == nullptr) {
            __android_log_print(ANDROID_LOG_INFO, "xiabo", "fopen /sdcard/maps error %s", strerror(errno));
        } else {
            fclose(maps);
        }
        __android_log_print(ANDROID_LOG_INFO, "xiabo", "----------------------------");
        fclose(file);
    } else {
        __android_log_print(ANDROID_LOG_INFO, "xiabo", "fopen /proc/self/maps error %s", strerror(errno));
    }
    return env->NewStringUTF(hello.c_str());
}
