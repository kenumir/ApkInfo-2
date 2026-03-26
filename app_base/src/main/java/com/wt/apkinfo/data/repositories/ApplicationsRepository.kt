package com.wt.apkinfo.data.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import com.wt.apkinfo.base.R
import com.wt.apkinfo.data.ApkFileEntryInfo
import com.wt.apkinfo.data.ApplicationDetailsInfo
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.era.ERA
import com.wt.apkinfo.proto.FilterAppType
import com.wt.apkinfo.proto.FilterInstaller
import com.wt.apkinfo.proto.FilterTargetSdk
import com.wt.apkinfo.proto.ListSortOrder
import com.wt.apkinfo.proto.StringUtil
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ApplicationsRepository(private val context: Context) {

    private var res = context.resources
    private var pkg: PackageManager? = try {
        context.applicationContext.packageManager
    } catch (e: Exception) {
        null
    }

    fun getApkFiles(pkgName: String): List<ApkFileEntryInfo> {
        val results: ArrayList<ApkFileEntryInfo> = ArrayList()
        pkg?.let{ pit ->
            ERA.log("ApplicationsRepository.getApkFiles: pkgName=$pkgName")
            try {
                try {
                    pit.getApplicationInfo(pkgName, 0)
                } catch (e: java.lang.Exception) {
                    null
                }?.let { appInfo ->
                    val apk = File(appInfo.publicSourceDir)
                    apk.parent?.let {
                        File(it).listFiles { _, name ->
                            name?.endsWith(".apk") ?: false
                        }?.forEach { itf ->
                            results.add(ApkFileEntryInfo.fromFile(itf))
                        }
                    } ?: run {
                        results.add(ApkFileEntryInfo(apk.name, apk.absolutePath, apk.length()))
                    }
                }
            } catch (e: Exception) {
                //ERA.logException(e)
            }
        }
        return results
    }

    fun createZipArchive(pkg: String, versionName: String?, versionCode: Int): File {
        val versionNameFixed = versionName
            ?.replace("[^\\p{ASCII}]", "")
            ?.replace(" ", "") ?: "unknown"
            
        val dir = File(context.cacheDir, "archives").apply { mkdirs() }
        val file = File(dir, "$pkg-$versionNameFixed-$versionCode.zip")
        
        val buffSize = 8192
        val dest = FileOutputStream(file)
        val out = ZipOutputStream(BufferedOutputStream(dest))
        val buffer = ByteArray(buffSize)

        // Add APKs
        getApkFiles(pkg).forEach { singleApk ->
            singleApk.fullPath?.let { fp ->
                val f = File(fp)
                if (f.exists()) {
                    FileInputStream(f).use { fi ->
                        val entry = ZipEntry(singleApk.name).apply { time = f.lastModified() }
                        val origin = BufferedInputStream(fi, buffSize)
                        out.putNextEntry(entry)
                        var count: Int
                        while (origin.read(buffer, 0, buffSize).also { count = it } != -1) {
                            out.write(buffer, 0, count)
                        }
                        out.closeEntry()
                    }
                }
            }
        }

        // Add info.txt
        val appInfo = getApplicationDetailsInfo(pkg)
        val infoText = "Package: ${appInfo.pkg}\n" +
                "Name: ${appInfo.name}\n" +
                "Version Name: ${appInfo.versionName}\n" +
                "Version Code: ${appInfo.versionCode}\n" +
                "Signature: ${appInfo.signature}\n" +
                "Installer Package: ${appInfo.installerPackage}\n" +
                "Min SDK: ${appInfo.sdkMin}\n" +
                "Target SDK: ${appInfo.sdkTarget}"
        
        val entry = ZipEntry("info.txt").apply { time = System.currentTimeMillis() }
        out.putNextEntry(entry)
        val bytes = infoText.toByteArray(Charset.forName("UTF-8"))
        out.write(bytes, 0, bytes.size)
        out.closeEntry()

        out.close()
        return file
    }

    @SuppressLint("DefaultLocale")
    fun getAppList(query: String?, appType: FilterAppType, sortOrder: ListSortOrder, installer: FilterInstaller, targetSdk: FilterTargetSdk): List<ApplicationEntryInfo> {
        val results: ArrayList<ApplicationEntryInfo> = ArrayList()
        var id = 0L
        try {
            Locale.getDefault().toString().lowercase().let { lang ->
                if (lang.startsWith("ru_") || lang.startsWith("be_")) {
                    results.add(
                        ApplicationEntryInfo(
                            id,
                            null,
                            res.getString(R.string.no_results),
                            null,
                            0,
                            FilterAppType.ALL,
                            FilterInstaller.ALL,
                            0
                        )
                    )
                    return results;
                }
            }

            pkg?.getInstalledPackages(0)?.forEach { it ->
                val launcher = pkg?.getLaunchIntentForPackage(it.packageName)
                var appName: String? = null
                val appIcon = "app://${it.packageName}"
                val pkgName = it.packageName
                val date = it.lastUpdateTime
                var appT: FilterAppType = FilterAppType.ALL
                val installerPkgRes: FilterInstaller

                val info = it.applicationInfo
                val isSystemApp = info != null && info.flags and ApplicationInfo.FLAG_SYSTEM != 0
                val isDebuggable = info != null && info.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
                val targetSdkRes = info?.targetSdkVersion ?: 0

                val installerPkg = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        pkg?.getInstallSourceInfo(pkgName)?.let {
                            if (it.installingPackageName.isNullOrEmpty()) {
                                null
                            } else {
                                it.installingPackageName
                            }
                        } ?: run {
                            null
                        }
                    } else {
                        pkg?.getInstallerPackageName(pkgName)?.let {
                            it.ifEmpty {
                                null
                            }
                        } ?: run {
                            null
                        }
                    }
                } catch (e: java.lang.Exception) {
                    null
                }

                installerPkgRes = when (installerPkg) {
                    "com.android.vending" -> FilterInstaller.PLAY_STORE
                    "com.huawei.appmarket" -> FilterInstaller.APP_GALLERY
                    "" -> FilterInstaller.EMPTY
                    null -> FilterInstaller.EMPTY
                    else -> FilterInstaller.ALL
                }

                if (launcher != null) {
                    appT = FilterAppType.USER
                    val activityList = pkg?.queryIntentActivities(launcher, 0)
                    activityList?.let {
                        appName = if (it.size > 0) {
                            pkg?.let { it1 ->
                                it[0].activityInfo?.loadLabel(it1)
                            }.toString()
                        } else {
                            ""
                        }
                    }
                } else {
                    if (appType == FilterAppType.ALL) {
                        appT = FilterAppType.ALL
                        try {
                            pkg?.getApplicationInfo(it.packageName, 0)?.let {
                                appName = pkg?.getApplicationLabel(it).toString()
                            } ?: run {
                                appName = ""
                            }
                        } catch (e: Exception) {
                            appName = ""
                        }
                    } else {
                        appName = ""
                    }
                }

                if (isSystemApp) {
                    appT = FilterAppType.SYSTEM
                } else if (isDebuggable) {
                    appT = FilterAppType.DEBUG
                }

                if (appType == FilterAppType.ALL) {
                    //
                } else if (appType == FilterAppType.DEBUG && appT != FilterAppType.DEBUG) {
                    appName = ""
                } else if (appType == FilterAppType.SYSTEM && appT != FilterAppType.SYSTEM) {
                    appName = ""
                } else if (appType == FilterAppType.USER && appT != FilterAppType.USER) {
                    appName = ""
                }

                when (targetSdk) {
                    FilterTargetSdk.ALL -> {
                        // all apps
                    }
                    FilterTargetSdk.API_35 -> {
                        if (targetSdkRes != 35) {
                            appName = ""
                        }
                    }
                    FilterTargetSdk.API_34 -> {
                        if (targetSdkRes != 34) {
                            appName = ""
                        }
                    }
                    FilterTargetSdk.API_33 -> {
                        if (targetSdkRes != 33) {
                            appName = ""
                        }
                    }
                    FilterTargetSdk.API_36 -> {
                        if (targetSdkRes != 36) {
                            appName = ""
                        }
                    }
                }

                if (installer != FilterInstaller.ALL) {
                    if (installerPkgRes != installer) {
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
                                date,
                                appT,
                                installerPkgRes,
                                targetSdkRes
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
                                    date,
                                    appT,
                                    installerPkgRes,
                                    targetSdkRes
                                )
                            )
                        }
                    }
                }
            }

            if (results.size == 0) {
                results.add(
                    ApplicationEntryInfo(
                        id,
                        null,
                        res.getString(R.string.no_results),
                        null,
                        0,
                        FilterAppType.ALL,
                        FilterInstaller.ALL,
                        0
                    )
                )
            }
        } catch (e: Exception) {
            //ERA.logException(e)
        }
        return when (sortOrder) {
            ListSortOrder.PACKAGE -> {
                results
                    .sortedWith(compareBy { it2 -> it2.pkg })
            }
            ListSortOrder.DATE -> {
                results
                    .sortedWith(compareByDescending { it2 -> it2.date })
            }
            else -> {
                results
                    .sortedWith(compareBy { it2 -> it2.name })
            }
        }
    }

    @SuppressLint("DefaultLocale")
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
                result.launcherIntentTv = pit.getLeanbackLaunchIntentForPackage(packageName)
                result.launcherIntentTv?.let {
                    val activityList = pit.queryIntentActivities(it, 0)
                    activityList.let { ait ->
                        val info = ait[0]
                        result.name = info.activityInfo?.loadLabel(pit).toString()
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    result.sdkMin = pi.applicationInfo?.minSdkVersion ?: 0
                }
                result.sdkTarget = pi.applicationInfo?.targetSdkVersion ?: 0
                result.pkg = packageName

                val installerPkg = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        pit.getInstallSourceInfo(packageName).installingPackageName
                    } else {
                        @Suppress("DEPRECATION")
                        pit.getInstallerPackageName(packageName)
                    }
                } catch (e: Exception) {
                    null
                }
                result.installerPackage = if (installerPkg.isNullOrEmpty()) {
                    res.getString(R.string.not_set)
                } else {
                    installerPkg
                }

                result.versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pi.longVersionCode.toInt()
                } else {
                    pi.versionCode
                }
                result.versionName = pi.versionName


                val sigBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val pInfo = pit.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    pInfo?.signingInfo?.let { si ->
                        if (si.hasMultipleSigners()) {
                            si.apkContentsSigners?.firstOrNull()?.toByteArray()
                        } else {
                            si.signingCertificateHistory?.firstOrNull()?.toByteArray()
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val pInfo = pit.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                    @Suppress("DEPRECATION")
                    pInfo?.signatures?.firstOrNull()?.toByteArray()
                }
                sigBytes?.let {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(it)
                    val s = StringBuilder()
                    for (b in md.digest()) {
                        s.append(":").append(String.format("%02x", b))
                    }
                    result.signature = s.substring(1).toString()
                }
                result.timeInstall = pi.firstInstallTime
                result.timeUpdate = pi.lastUpdateTime
                result.directories.add(res.getString(R.string.data_directory) + "\n" + pi.applicationInfo?.dataDir)
                result.directories.add(res.getString(R.string.native_lib_directory) + "\n" + pi.applicationInfo?.nativeLibraryDir)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    result.directories.add(res.getString(R.string.protected_data_directory) + "\n" + pi.applicationInfo?.deviceProtectedDataDir)
                }
                result.directories.add(res.getString(R.string.apk_directory) + "\n" + pi.applicationInfo?.publicSourceDir)

                pi.applicationInfo?.nativeLibraryDir?.let {
                    if (it.isNotEmpty()) {
                        File(pi.applicationInfo?.nativeLibraryDir!!).listFiles()?.forEach { itf ->
                            result.nativeLibraries.add(StringUtil.formatFileSize(itf.length()) + "\n" + itf.absolutePath)
                        }
                    }
                }

                pit.getPackageInfo(packageName, PackageManager.GET_META_DATA).applicationInfo?.metaData?.let{
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
                        result.services.add(ai.name + "\n" + ai.loadLabel(pit).toString())
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
                ERA.logException(e)
            }
        }
        return result
    }

}
