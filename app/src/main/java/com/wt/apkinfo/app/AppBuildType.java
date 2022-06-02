package com.wt.apkinfo.app;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({
	AppBuildType.GOOGLE,
	AppBuildType.HUAWEI,
	AppBuildType.APK,
})
public @interface AppBuildType {
	String GOOGLE = "google";
	String HUAWEI = "huawei";
	String APK = "apk";
}
