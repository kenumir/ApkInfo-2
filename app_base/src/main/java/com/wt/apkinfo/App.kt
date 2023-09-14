package com.wt.apkinfo

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.multidex.MultiDex
import com.bugsnag.android.Bugsnag
import com.wt.apkinfo.base.BuildConfig
import com.wt.apkinfo.era.ERA
import com.wt.apkinfo.proto.Themes
import com.wt.userinfo.UserInfo

class App : Application () {

    private lateinit var mUserInfo: UserInfo

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

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
        Bugsnag.start(this);
        ERA.setString("Build For Market", BuildConfig.BUILD_FOR_MARKET)

        Themes.setupTheme(this)

        mUserInfo = UserInfo.setup(this, BuildConfig.APP_VERSION_NAME, null)
        Bugsnag.setUser(mUserInfo.id, null, null)
    }

    fun getUserInfo(): UserInfo {
        return mUserInfo;
    }

}