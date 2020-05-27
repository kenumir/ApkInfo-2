package com.wt.apkinfo.era;

import android.content.Context;

import com.huawei.agconnect.crash.AGConnectCrash;

public class ERAImpl {

	public static void setString(String key, String value) {
		//Crashlytics.setString(key, value);
	}

	public static void testError(Context ctx) {
		AGConnectCrash.getInstance().testIt(ctx);
	}

	public static void logException(Throwable e) {
		//AGConnectCrash.getInstance().
	}

	public static void log(String s) {

	}

}
