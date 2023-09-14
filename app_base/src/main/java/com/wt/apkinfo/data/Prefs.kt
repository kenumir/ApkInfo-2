package com.wt.apkinfo.data

import android.content.Context
import android.content.SharedPreferences
import com.wt.apkinfo.proto.FilterAppType
import com.wt.apkinfo.proto.FilterInstaller
import com.wt.apkinfo.proto.ListSortOrder

class Prefs(private val ctx: Context) {
    private val pref: SharedPreferences
        get() = ctx.getSharedPreferences("com.wt.apkinfo.preferences", Context.MODE_PRIVATE)
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
    var listFilterAppType: FilterAppType
        get() = FilterAppType.valueOf(
            pref.getString("filter_app_type", FilterAppType.ALL.name) ?:
            FilterAppType.ALL.name
        )
        set(v) {
            pref.edit().putString("filter_app_type", v.name).apply()
        }
    var listFilterInstaller: FilterInstaller
        get() = FilterInstaller.valueOf(
            pref.getString("filter_installer", FilterInstaller.ALL.name) ?:
            FilterInstaller.ALL.name
        )
        set(v) {
            pref.edit().putString("filter_installer", v.name).apply()
        }

    var appDetailsOpenCounter: Int
        get() {
            val r = pref.getInt("app_details_open_counter", 1)
            pref.edit().putInt("app_details_open_counter", r + 1).apply()
            return r
        }
        set(v) {
            pref.edit().putInt("app_details_open_counter", v).apply()
        }

    var appTheme: Int
        get() {
            return pref.getInt("app_theme", 0)
        }
        set(v) {
            pref.edit().putInt("app_theme", v).apply()
        }
}