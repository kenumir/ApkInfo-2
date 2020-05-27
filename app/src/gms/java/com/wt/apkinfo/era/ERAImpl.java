package com.wt.apkinfo.era;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.wt.apkinfo.BuildConfig;

public class ERAImpl {

	public static void setString(String key, String value) {
		Crashlytics.setString(key, value);
	}

	public static void testError(Context ctx) {
		//AGConnectCrash.getInstance().testIt(ctx);
	}

	public static void logException(Throwable e) {
		Crashlytics.logException(e);
	}

	public static void log(String s) {

	}

}
