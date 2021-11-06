package com.wt.apkinfo.data.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import com.wt.apkinfo.R
import com.wt.apkinfo.data.ApkFileEntryInfo
import com.wt.apkinfo.data.ApplicationDetailsInfo
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.era.ERA
import com.wt.apkinfo.proto.ListSortOrder
import com.wt.apkinfo.proto.StringUtil
import java.io.File
import java.io.FilenameFilter
import java.security.MessageDigest
import java.util.*
import kotlin.collections.ArrayList

class ApplicationsRepository(ctx: Context) {

    private var res = ctx.resources
    private var pkg: PackageManager? = try {
        ctx.applicationContext.packageManager
    } catch (e: Exception) {
        null
    }

    fun getApkFiles(pkgName: String): List<ApkFileEntryInfo> {
        val results: ArrayList<ApkFileEntryInfo> = ArrayList()
        pkg?.let{ pit ->
            ERA.log("ApplicationsRepository.getApkFiles: pkgName=$pkgName")
            try {
                val appInfo = pit.getApplicationInfo(pkgName, 0)
                val apk = File(appInfo.publicSourceDir)
                apk.parent?.let {
                    File(it).listFiles(object : FilenameFilter {
                        override fun accept(dir: File?, name: String?): Boolean {
                            return name?.endsWith(".apk")!!
                        }
                    })?.forEach { itf ->
                        results.add(ApkFileEntryInfo.fromFile(itf))
                    }
                } ?: run {
                    results.add(ApkFileEntryInfo(apk.name, apk.absolutePath, apk.length()))
                }
            } catch (e: Exception) {
                ERA.logException(e)
            }
        }
        return results
    }

    @SuppressLint("DefaultLocale")
    fun getAppList(query: String?, showAllApps: Boolean, sortOrder: ListSortOrder): List<ApplicationEntryInfo> {
        val results: ArrayList<ApplicationEntryInfo> = ArrayList()
        var id = 0L
        try {
            //pkg?.getInstalledApplications(0)?.forEach { it ->
            pkg?.getInstalledPackages(0)?.forEach { it ->
                val launcher = pkg?.getLaunchIntentForPackage(it.packageName)
                var appName: String? = null
                val appIcon = "app://${it.packageName}"
                val pkgName = it.packageName
                val date = it.lastUpdateTime

                launcher?.let {
                    val activityList = pkg?.queryIntentActivities(launcher, 0)
                    activityList?.let {
                        val info = it[0]
                        appName = pkg?.let { it1 -> info.activityInfo?.loadLabel(it1) }.toString()
                    }
                } ?: run {
                    if (showAllApps) {
                        val info = pkg?.getApplicationInfo(it.packageName, 0)
                        info?.let {
                            appName = pkg?.getApplicationLabel(it).toString()
                        } ?: run {
                            appName = ""
                        }
                    } else {
                        appName = ""
                    }
                }

                id++
                if (!TextUtils.isEmpty(appName)) {
                    if (query == null || query.isEmpty()) {
                        results.add(
                            ApplicationEntryInfo(
                                id,
                                pkgName,
                                appName,
                                appIcon,
                                date
                            )
                        )
                    } else {
                        if (pkgName.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) || appName?.lowercase(
                                Locale.getDefault()
                            )
                                ?.contains(
                                    query.lowercase(Locale.getDefault())
                            )!!
                        ) {
                            results.add(
                                ApplicationEntryInfo(
                                    id,
                                    pkgName,
                                    appName,
                                    appIcon,
                                    date
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            ERA.logException(e)
        }
        return when (sortOrder) {
            ListSortOrder.PACKAGE -> {
                results
                    .sortedWith(compareBy { it2 -> it2.pkg })
            }
            ListSortOrder.DATE -> {
                results
                    .sortedWith(compareByDescending { it2 -> it2.date })
                    .sortedWith(compareBy { it2 -> it2.name })
            }
            else -> {
                results
                    .sortedWith(compareBy { it2 -> it2.name })
            }
        }
        //return results.sortedWith(compareBy { it2 -> it2.name })
    }

    @Suppress("DEPRECATION")
    @SuppressLint("PackageManagerGetSignatures")
    fun getApplicationDetailsInfo(packageName: String): ApplicationDetailsInfo {
        val result = ApplicationDetailsInfo()
        pkg?.let { pit ->
            try {
                val pi = pit.getPackageInfo(packageName, 0)
                val appInfo = pit.getApplicationInfo(packageName, 0)

                result.isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                result.isDebuggable = appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
                result.isLargeHeap = appInfo.flags and ApplicationInfo.FLAG_LARGE_HEAP != 0
                result.isSupportRtl = appInfo.flags and ApplicationInfo.FLAG_SUPPORTS_RTL != 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    result.isHwAccelerated = appInfo.flags and ApplicationInfo.FLAG_HARDWARE_ACCELERATED != 0
                }
                result.icon ="app://$packageName"

                result.launcherIntent = pit.getLaunchIntentForPackage(packageName)
                result.launcherIntent?.let {
                    val activityList = pit.queryIntentActivities(it, 0)
                    activityList.let { ait ->
                        val info = ait[0]
                        result.name = info.activityInfo?.loadLabel(pit).toString()
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
                result.directories.add(res.getString(R.string.apk_directory) + "\n" + pi.applicationInfo.publicSourceDir)

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
                        result.providers.add(
                            ai.name + "\n" + ai.loadLabel(pit).toString() + "\n" + ai.authority
                        )
                    }
                }

                pit.getPackageInfo(packageName, PackageManager.GET_RECEIVERS).receivers?.let{
                    for(ai in it) {
                        result.receivers.add(ai.name + "\n" + ai.loadLabel(pit).toString())
                    }
                }

                pit.getPackageInfo(
                    packageName,
                    PackageManager.GET_PERMISSIONS or PackageManager.GET_URI_PERMISSION_PATTERNS
                ).permissions?.let{
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
                pit.getPackageInfo(
                    packageName,
                    PackageManager.GET_PERMISSIONS or PackageManager.GET_URI_PERMISSION_PATTERNS
                ).requestedPermissions?.let{
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