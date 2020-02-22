package com.wt.apkinfo.data

import android.content.Intent
import android.graphics.drawable.Drawable

class ApplicationDetailsInfo(
    var pkg: String? = null,
    var name: String? = null,
    var versionName: String? = null,
    var signature: String? = null,
    var installerPackage: String? = null,
    var versionCode: Int = 0,
    var sdkMin: Int = 0,
    var sdkTarget: Int = 0,
    var timeInstall: Long = 0,
    var timeUpdate: Long = 0,
    var icon: String? = null,
    var launcherIntent: Intent? = null,
    var meta: ArrayList<String> = ArrayList(),
    var activities: ArrayList<String> = ArrayList(),
    var services: ArrayList<String> = ArrayList(),
    var providers: ArrayList<String> = ArrayList(),
    var receivers: ArrayList<String> = ArrayList(),
    var directories: ArrayList<String> = ArrayList(),
    var permissions: ArrayList<String> = ArrayList(),
    var sharedLibraries: ArrayList<String> = ArrayList(),
    var nativeLibraries: ArrayList<String> = ArrayList(),

    var isSystemApp: Boolean = false,
    var isDebuggable: Boolean = false,
    var isLargeHeap: Boolean = false,
    var isSupportRtl: Boolean = false,
    var isHwAccelerated: Boolean = false
) {
}