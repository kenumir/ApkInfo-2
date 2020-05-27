package com.wt.apkinfo.era;

import android.content.Context;

public class ERA {

	public static void setString(String key, String value) {
		ERAImpl.setString(key, value);
	}

	public static void testError(Context ctx) {
		ERAImpl.testError(ctx);
	}

	public static void logException(Throwable e) {
		ERAImpl.logException(e);
	}

	public static void log(String s) {
		ERAImpl.log(s);
	}
}
