#include <jni.h>

#ifndef _Included_com_turbovnc_rfb_TightDecoder
#define _Included_com_turbovnc_rfb_TightDecoder
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_turbovnc_rfb_TightDecoder_tjInitDecompress
  (JNIEnv *, jobject);

JNIEXPORT void JNICALL Java_com_turbovnc_rfb_TightDecoder_tjDecompress__J_3BI_3BIIIIIII
  (JNIEnv *, jobject, jlong, jbyteArray, jint, jbyteArray, jint, jint, jint, jint, jint, jint, jint);

JNIEXPORT void JNICALL Java_com_turbovnc_rfb_TightDecoder_tjDecompress__J_3BI_3IIIIIIII
  (JNIEnv *, jobject, jlong, jbyteArray, jint, jintArray, jint, jint, jint, jint, jint, jint, jint);

JNIEXPORT void JNICALL Java_com_turbovnc_rfb_TightDecoder_tjDestroy
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
