package com.wt.apkinfo.proto;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

public class Utils {

	public static boolean isTV(@NonNull Context ctx) {
		try {
			// source: https://stackoverflow.com/questions/27138838/how-can-i-check-if-an-app-is-running-on-an-android-tv
			UiModeManager uiModeManager = (UiModeManager) ctx.getSystemService(Context.UI_MODE_SERVICE);
			return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
			//return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
			//    ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEVISION);
		} catch (Exception e) {
			// ignore
		}
		return false;
	}

}
