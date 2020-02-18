package com.wt.apkinfo.data

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
    var icon: Drawable? = null,
    var meta: ArrayList<String>? = ArrayList(),
    var activities: ArrayList<String>? = ArrayList()
) {
}