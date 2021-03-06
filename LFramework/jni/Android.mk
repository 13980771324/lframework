
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libLFramework

LOCAL_MODULE_FILENAME := libLFramework

LOCAL_SHARED_LIBRARIES += libandroid_runtime

LOCAL_SRC_FILES := \
src/main.cpp \
src/LuaAPP.cpp \
\
lua/lapi.c \
lua/lauxlib.c \
lua/lbaselib.c \
lua/lcode.c \
lua/ldblib.c \
lua/ldebug.c \
lua/ldo.c \
lua/ldump.c \
lua/lfunc.c \
lua/lgc.c \
lua/linit.c \
lua/liolib.c \
lua/llex.c \
lua/lmathlib.c \
lua/lmem.c \
lua/loadlib.c \
lua/lobject.c \
lua/lopcodes.c \
lua/loslib.c \
lua/lparser.c \
lua/lstate.c \
lua/lstring.c \
lua/lstrlib.c \
lua/ltable.c \
lua/ltablib.c \
lua/ltm.c \
lua/lua.c \
lua/lundump.c \
lua/lvm.c \
lua/lzio.c \
lua/print.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/src \
$(LOCAL_PATH)/lua \

LOCAL_LDLIBS :=-llog -lm -landroid

include $(BUILD_SHARED_LIBRARY)