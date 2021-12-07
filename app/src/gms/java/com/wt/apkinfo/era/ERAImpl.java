package com.wt.apkinfo.era;

import android.content.Context;

import com.bugsnag.android.Bugsnag;
import com.google.firebase.crashlytics.FirebaseCrashlytics;


public class ERAImpl {

	public static void setString(String key, String value) {
		try {
			FirebaseCrashlytics.getInstance().setCustomKey(key, value);
		} catch (Exception e) {
			// ignore
		}
	}

	public static void testError(Context ctx) {
		//AGConnectCrash.getInstance().testIt(ctx);
		Bugsnag.notify(new RuntimeException("Test error"));
	}

	public static void logException(Throwable e) {
		try {
			FirebaseCrashlytics.getInstance().recordException(e);
		} catch (Exception ee) {
			// ignore
		}
		Bugsnag.notify(e);
	}

	public static void log(String s) {
		try {
			FirebaseCrashlytics.getInstance().log(s);
		} catch (Exception ee) {
			// ignore
		}
		Bugsnag.leaveBreadcrumb(s);
	}

}
