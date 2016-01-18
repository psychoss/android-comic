#include <jni.h>
#include "buffer.h"
#include "connect.h"
#include <android/log.h>

#define TAG "Comic"
#define LOGW(...) (__android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__))
#define LOGD(...) (__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))
#define LOGV(...) (__android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__))

jstring
Java_euphoria_psycho_comic_util_Utilities_request(JNIEnv *env, jstring host, jstring path) {
    const char *n_host = (*env)->GetStringUTFChars(env, host, 0);
    const char *n_path = (*env)->GetStringUTFChars(env, path, 0);
    LOGW("%s",n_host);



    Buffer *response = buffer_alloc(255);

    int status = 0;
    int sockfd = 0;
    struct addrinfo *res = NULL;

    status = init_connection(n_host, "80", &res);
    error_unless(status == 0, "Could not resolve host: %s\n", gai_strerror(status));

    jstring result1 = (*env)->NewStringUTF(env, n_host);

    return result1;
    sockfd = make_connection(res);
    error_unless(sockfd > 0, "Could not make connection to '");

    status = make_request(sockfd, n_host, n_path);
    error_unless(status > 0, "Sending request failed");

    status = fetch_response(sockfd, &response, RECV_SIZE);
    error_unless(status >= 0, "Fetching response failed");

    printf("%s\n", response->contents);

    close(sockfd);

    freeaddrinfo(res);
    jstring result = (*env)->NewStringUTF(env, response->contents);
    buffer_free(response);
    (*env)->ReleaseStringUTFChars(env, host, n_host);
    (*env)->ReleaseStringUTFChars(env, path, n_path);

    return result;

    error:
    if (sockfd > 0) { close(sockfd); }
    if (res != NULL) { freeaddrinfo(res); }
    (*env)->ReleaseStringUTFChars(env, host, n_host);
    (*env)->ReleaseStringUTFChars(env, path, n_path);
    return (*env)->NewStringUTF(env, "Error");


    buffer_free(response);
//    return (*env)->NewStringUTF(env, "Hello from JNI !  Compiled with ABI "
//    ABI
//    ".");
}