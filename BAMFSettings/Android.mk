# Copyright 2007-2011 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
chips_dir := ../../../frameworks/ex/chips/res
sliding_dir := ../../../frameworks/ex/slidingmenu/res
res_dir := $(chips_dir) \
		$(sliding_dir) \
		res

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)\
    $(call all-java-files-under, ../BAMFUtils/src)\
	../BAMFUtils/src/com/bamf/bamfutils/services/IRootService.aidl
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dir))
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.slidingmenu.lib:com.android.ex.chips

LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true

LOCAL_STATIC_JAVA_LIBRARIES := android-common-chips \
		android-common-slidingmenu \
		android-support-v13 \
        android-support-v4 \

LOCAL_PACKAGE_NAME := BAMFSettings
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)
