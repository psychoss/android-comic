LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := app
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_LDLIBS    := -lm -llog # -ljnigraphics

LOCAL_SRC_FILES := \
	/home/psycho/Android/Project/Comic3/app/src/main/jni/buffer.c \
	/home/psycho/Android/Project/Comic3/app/src/main/jni/app.c \
	/home/psycho/Android/Project/Comic3/app/src/main/jni/connect.c \

LOCAL_C_INCLUDES += /home/psycho/Android/Project/Comic3/app/src/main/jni
LOCAL_C_INCLUDES += /home/psycho/Android/Project/Comic3/app/src/debug/jni

include $(BUILD_SHARED_LIBRARY)