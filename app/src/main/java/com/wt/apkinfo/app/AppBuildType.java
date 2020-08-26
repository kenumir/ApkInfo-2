package com.wt.apkinfo.app;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({
	AppBuildType.GOOGLE,
	AppBuildType.HUAWEI,
})
public @interface AppBuildType {
	String GOOGLE = "google";
	String HUAWEI = "huawei";
}
