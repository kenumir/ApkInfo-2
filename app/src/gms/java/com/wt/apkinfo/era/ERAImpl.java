package com.wt.apkinfo.era;

import android.content.Context;

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
	}

	public static void logException(Throwable e) {
		try {
			FirebaseCrashlytics.getInstance().recordException(e);
		} catch (Exception ee) {
			// ignore
		}
	}

	public static void log(String s) {
		try {
			FirebaseCrashlytics.getInstance().log(s);
		} catch (Exception ee) {
			// ignore
		}
	}

}
