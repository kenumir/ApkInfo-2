package com.wt.apkinfo.proto

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.wt.apkinfo.data.Prefs

object Themes {
    fun setupTheme(ctx: Context) {
        when (Prefs(ctx).appTheme) {
            0 -> {
                if (Build.VERSION.SDK_INT >= 29) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            2 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }
}