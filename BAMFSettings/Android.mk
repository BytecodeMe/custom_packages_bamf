# Copyright 2007-2011 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
chips_dir := ../../../frameworks/ex/chips/res
res_dir := $(chips_dir) res

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)\
    $(call all-java-files-under, ../BAMFUtils/src)\
	../BAMFUtils/src/com/bamf/BAMFUtils/services/IRootService.aidl
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dir))

LOCAL_STATIC_JAVA_LIBRARIES := android-common-chips

LOCAL_PACKAGE_NAME := BAMFSettings
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)
