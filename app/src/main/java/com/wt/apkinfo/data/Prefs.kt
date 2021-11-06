package com.wt.apkinfo.data

import android.content.Context
import android.content.SharedPreferences
import com.wt.apkinfo.proto.ListSortOrder

class Prefs(ctx: Context) {
    private val pref: SharedPreferences =
        ctx.getSharedPreferences("com.wt.apkinfo.preferences", Context.MODE_PRIVATE)
    var listSortOrder: ListSortOrder
        get() = when (pref.getInt("sort_order", 0)) {
            1 -> ListSortOrder.DATE
            2 -> ListSortOrder.PACKAGE
            else -> ListSortOrder.NAME
        }
        set(value) {
            when (value) {
                ListSortOrder.DATE -> {
                    pref.edit().putInt("sort_order", 1).apply()
                }
                ListSortOrder.PACKAGE -> {
                    pref.edit().putInt("sort_order", 2).apply()
                }
                else -> {
                    pref.edit().putInt("sort_order", 0).apply()
                }
            }
        }
    var allApps: Int
        get() = pref.getInt("all_apps", 0)
        set(v) {
            pref.edit().putInt("all_apps", v).apply()
        }

}