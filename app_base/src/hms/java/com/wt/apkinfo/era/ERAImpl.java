package com.wt.apkinfo.era;

import android.content.Context;

import com.bugsnag.android.Bugsnag;


public class ERAImpl {

	public static void setString(String key, String value) {
		// Bugsnag does not have a simple setString for custom keys in the same way, 
		// but we can use addMetadata if needed. For now, matching gms/apk flavor.
	}

	public static void testError(Context ctx) {
		//AGConnectCrash.getInstance().testIt(ctx);
		Bugsnag.notify(new RuntimeException("Test error"));
	}

	public static void logException(Throwable e) {
		Bugsnag.notify(e);
	}

	public static void log(String s) {
		Bugsnag.leaveBreadcrumb(s);
	}

}
