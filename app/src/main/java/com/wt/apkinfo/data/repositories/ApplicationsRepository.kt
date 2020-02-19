package com.wt.apkinfo.data.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import com.wt.apkinfo.R
import com.wt.apkinfo.data.ApplicationDetailsInfo
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.proto.StringUtil
import java.io.File
import java.security.MessageDigest


class ApplicationsRepository(ctx: Context) {

    private var res = ctx.resources
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
                val appInfo = pit.getApplicationInfo(packageName, 0)

                result.isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                result.isDebuggable = appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
                result.isLargeHeap = appInfo.flags and ApplicationInfo.FLAG_LARGE_HEAP != 0

                result.launcherIntent = pit.getLaunchIntentForPackage(packageName)
                result.launcherIntent?.let {
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

                pit.getInstallerPackageName(packageName)?.let {
                    result.installerPackage = if (it.isEmpty()) { res.getString(R.string.not_set) } else { it }
                } ?: run {
                    result.installerPackage = res.getString(R.string.not_set)
                }

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
                result.directories.add(res.getString(R.string.data_directory) + "\n" + pi.applicationInfo.dataDir)
                result.directories.add(res.getString(R.string.native_lib_directory) + "\n" + pi.applicationInfo.nativeLibraryDir)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    result.directories.add(res.getString(R.string.protected_data_directory) + "\n" + pi.applicationInfo.deviceProtectedDataDir)
                }

                pi.applicationInfo.nativeLibraryDir?.let {
                    if (it.isNotEmpty()) {
                        File(pi.applicationInfo.nativeLibraryDir).listFiles()?.forEach { itf ->
                            result.nativeLibraries.add(StringUtil.formatFileSize(itf.length()) + "\n" + itf.absolutePath)
                        }
                    }
                }

                pit.getPackageInfo(packageName, PackageManager.GET_META_DATA).applicationInfo.metaData?.let{
                    val keys = it.keySet()
                    for(key in keys) {
                        result.meta.add(key + "\n" + it[key].toString())
                    }
                }

                pit.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).activities?.let{
                    for(ai in it) {
                        result.activities.add(ai.name + "\n" + ai.loadLabel(pit).toString())
                    }
                }

                pit.getPackageInfo(packageName, PackageManager.GET_SERVICES).services?.let{
                    for(ai in it) {
                        result.activities.add(ai.name + "\n" + ai.loadLabel(pit).toString())
                    }
                }

                pit.getPackageInfo(packageName, PackageManager.GET_PROVIDERS).providers?.let{
                    for(ai in it) {
                        result.providers.add(ai.name + "\n" + ai.loadLabel(pit).toString() + "\n" + ai.authority)
                    }
                }

                pit.getPackageInfo(packageName, PackageManager.GET_RECEIVERS).receivers?.let{
                    for(ai in it) {
                        result.receivers.add(ai.name + "\n" + ai.loadLabel(pit).toString())
                    }
                }

                pit.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS or PackageManager.GET_URI_PERMISSION_PATTERNS).permissions?.let{
                    for(ai in it) {
                        val label = ai.loadLabel(pit).toString()
                        val perm = ai.name
                        if (perm == label) {
                            result.permissions.add(perm)
                        } else {
                            result.permissions.add(label + "\n" + perm)
                        }
                    }
                }
                pit.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS or PackageManager.GET_URI_PERMISSION_PATTERNS).requestedPermissions?.let{
                    for(ai in it) {
                        result.permissions.add(ai)
                    }
                }

                pit.getApplicationInfo(packageName, PackageManager.GET_SHARED_LIBRARY_FILES).sharedLibraryFiles?.let{
                    for(ai in it) {
                        result.sharedLibraries.add(StringUtil.formatFileSize(File(ai).length()) + "\n" + ai)
                    }
                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

}