LOCAL_PATH := $(call my-dir)

# Edit this line

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    GSTREAMER_ROOT_ANDROID :=/home/renan/jfl/gstreamer-1.0-android-armv7-1.6.1
else
    GSTREAMER_ROOT_ANDROID :=/home/renan/jfl/gstreamer-1.0-android-arm-nodebug-1.6.1
endif

SHELL := PATH=/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin /bin/bash

include $(CLEAR_VARS)

LOCAL_MODULE    := audiostreamer
LOCAL_SRC_FILES := audiostreamer.c
LOCAL_SHARED_LIBRARIES := gstreamer_android
LOCAL_LDLIBS := -llog -landroid
include $(BUILD_SHARED_LIBRARY)



ifndef GSTREAMER_ROOT
ifndef GSTREAMER_ROOT_ANDROID
$(error GSTREAMER_ROOT_ANDROID is not defined!)
endif
GSTREAMER_ROOT        := $(GSTREAMER_ROOT_ANDROID)
endif
GSTREAMER_NDK_BUILD_PATH  := $(GSTREAMER_ROOT)/share/gst-android/ndk-build


include $(GSTREAMER_NDK_BUILD_PATH)/plugins.mk
GSTREAMER_PLUGINS         := $(GSTREAMER_PLUGINS_CORE) $(GSTREAMER_PLUGINS_PLAYBACK) $(GSTREAMER_PLUGINS_CODECS) $(GSTREAMER_PLUGINS_NET) $(GSTREAMER_PLUGINS_SYS) $(GSTREAMER_PLUGINS_CODECS_RESTRICTED)
G_IO_MODULES              := gnutls
GSTREAMER_EXTRA_DEPS      := gstreamer-video-1.0

include $(GSTREAMER_NDK_BUILD_PATH)/gstreamer-1.0.mk
