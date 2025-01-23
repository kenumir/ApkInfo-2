package com.wt.apkinfo.era;

import android.content.Context;

import com.bugsnag.android.Bugsnag;


public class ERAImpl {

	public static void setString(String key, String value) {

	}

	public static void testError(Context ctx) {
		Bugsnag.notify(new RuntimeException("Test error"));
	}

	public static void logException(Throwable e) {
		Bugsnag.notify(e);
	}

	public static void log(String s) {
		Bugsnag.leaveBreadcrumb(s);
	}

}
