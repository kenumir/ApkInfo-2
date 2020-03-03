package com.wt.apkinfo

import android.app.Application
import android.os.Build
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import com.crashlytics.android.Crashlytics
import com.wt.userinfo.UserInfo

class App : Application() {

    private lateinit var mUserInfo: UserInfo

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog() //.penaltyDeath()
                    .build()
            )
        }
        super.onCreate()
        Crashlytics.setString("Build For Market", BuildConfig.BUILD_FOR_MARKET)
        if (Build.VERSION.SDK_INT >= 29) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
        mUserInfo = UserInfo.setup(this, BuildConfig.VERSION_NAME)
    }

    fun getUserInfo(): UserInfo {
        return mUserInfo;
    }

}