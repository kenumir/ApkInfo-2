package com.wt.apkinfo.data.repositories

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import com.wt.apkinfo.data.ApplicationEntryInfo


class ApplicationsRepository(ctx: Context) {

    private var pkg: PackageManager?

    init {
        try {
            pkg = ctx.applicationContext.packageManager
        } catch (e: Exception) {
            pkg = null
        }
    }

    @SuppressLint("DefaultLocale")
    fun getAppList(query: String?): List<ApplicationEntryInfo> {
        //val intentFiler = Intent(Intent.ACTION_MAIN, null)
        //    .addCategory(Intent.CATEGORY_LAUNCHER)
        val installedAppList = pkg?.getInstalledApplications(0) // pkg?.queryIntentActivities(intentFiler, 0)
        val results: ArrayList<ApplicationEntryInfo> = ArrayList()
        var id = 0L
        installedAppList?.forEach { it ->
            val launcher = pkg?.getLaunchIntentForPackage(it.packageName)
            var appName: String? = null
            var appIcon: Drawable? = null
            val pkgName = it.packageName

            if (launcher != null) {
                val activityList = pkg?.queryIntentActivities(launcher, 0)
                activityList?.let {
                    val info = it.get(0)
                    appName = pkg?.let { it1 -> info.activityInfo?.loadLabel(it1) }.toString()
                    appIcon = info.activityInfo?.loadIcon(pkg)

                }
            } else {
                appName = ""
            }

            id++
            if (appName?.isEmpty()!!) {
                //Log.w("tests", "No default activity for package `$pkgName`")
            } else {
                //Log.i("tests", "Add app pkg=`$pkgName`, name=`$appName`, icon=`$appIcon`, query=`$query`")

                if (query == null || query.isEmpty()) {
                    results.add(
                        ApplicationEntryInfo(
                            id,
                            pkgName,
                            appName,
                            appIcon
                        )
                    )
                } else {
                    if (pkgName.toLowerCase().contains(query.toLowerCase()) || appName?.toLowerCase()?.contains(query.toLowerCase())!!) {
                        results.add(
                            ApplicationEntryInfo(
                                id,
                                pkgName,
                                appName,
                                appIcon
                            )
                        )
                    }
                }
            }
        }
        return results.sortedWith(compareBy { it2 -> it2.name })
    }

}