package com.wt.apkinfo.data.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.wt.apkinfo.data.ApplicationDetailsInfo
import com.wt.apkinfo.data.ApplicationEntryInfo
import java.security.MessageDigest


class ApplicationsRepository(ctx: Context) {

    private var pkg: PackageManager? = try {
        ctx.applicationContext.packageManager
    } catch (e: Exception) {
        null
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
                    val info = it[0]
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

    fun getApplicationDetailsInfo(packageName: String): ApplicationDetailsInfo {
        val result = ApplicationDetailsInfo()
        pkg?.let { pit ->
            try {
                val pi = pit.getPackageInfo(packageName, 0)
                pit.getLaunchIntentForPackage(packageName)?.let {
                    val activityList = pit.queryIntentActivities(it, 0)
                    activityList.let { ait ->
                        val info = ait[0]
                        result.name = info.activityInfo?.loadLabel(pit).toString()
                        result.icon = info.loadIcon(pit)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    result.sdkMin = pi.applicationInfo.minSdkVersion
                }
                result.sdkTarget = pi.applicationInfo.targetSdkVersion
                result.pkg = packageName
                result.installerPackage = pit.getInstallerPackageName(packageName)
                result.versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pi.longVersionCode.toInt()
                } else {
                    pi.versionCode
                }
                result.versionName = pi.versionName


                val pInfo = pit.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                pInfo?.let {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(it.signatures[0].toByteArray())
                    val s = StringBuilder()
                    for (b in md.digest()) {
                        s.append(":").append(String.format("%02x", b))
                    }
                    result.signature = s.substring(1).toString()
                }
                result.timeInstall = pi.firstInstallTime
                result.timeUpdate = pi.lastUpdateTime
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            //pit.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        }
        return result
    }

}