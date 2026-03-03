package com.wt.apkinfo.data.models

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wt.apkinfo.base.BuildConfig
import com.wt.apkinfo.data.repositories.ApplicationsRepository
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArchiveViewModel(application: Application) : AndroidViewModel(application) {

    private val data = MutableLiveData<File>()
    private val progress = MutableLiveData(false)
    private val app = application
    private val appRepository: ApplicationsRepository = ApplicationsRepository(application)
    private var dataResult: String? = null

    fun getProgress(): MutableLiveData<Boolean> {
        return progress
    }

    fun startMakeArchive(pkg: String, versionName: String?, version: Int) {
        progress.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val versionNameFixed = versionName
                    ?.replace("[^\\p{ASCII}]", "")
                    ?.replace(" ", "")
                val dir = File(app.cacheDir, "archives").apply {
                    mkdirs()
                }
                val buffSize = 8192
                val file = File(dir, "$pkg-$versionNameFixed-$version.zip")
                val dest = FileOutputStream(file)
                val out = ZipOutputStream(BufferedOutputStream(dest))
                val data = ByteArray(buffSize)
                appRepository.getApkFiles(pkg).forEach { singleApk ->
                    singleApk.fullPath?.let { fp ->
                        val f = File(fp)
                        val fi = FileInputStream(f)
                        val entry = ZipEntry(singleApk.name).also {
                            it.time = f.lastModified()
                        }
                        val origin = BufferedInputStream(fi, buffSize)
                        out.putNextEntry(entry)
                        var count: Int
                        while (origin.read(data, 0, buffSize).also { count = it } != -1) {
                            out.write(data, 0, count)
                        }
                        fi.close()
                    }
                }
                appRepository.getApplicationDetailsInfo(pkg).let { appInfo ->
                    val infoText = "Package: ${appInfo.pkg}\n" +
                            "Name: ${appInfo.name}\n" +
                            "Version Name: ${appInfo.versionName}\n" +
                            "Version Code: ${appInfo.versionCode}\n" +
                            "Signature (MD5): ${appInfo.signature}\n" +
                            "Installer Package: ${appInfo.installerPackage}\n" +
                            "Min SDK: ${appInfo.sdkMin}\n" +
                            "Target SDK: ${appInfo.sdkTarget}"
                    val entry = ZipEntry("info.txt").also {
                        it.time = System.currentTimeMillis()
                    }
                    out.putNextEntry(entry)
                    out.write(infoText.toByteArray(Charset.forName("UTF-8")), 0, infoText.length)
                }
                out.close()
                dataResult = file.name
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e("apkinfo", "e=$e")
                }
            } finally {
                progress.postValue(false)
            }
        }
    }

    fun getLastDataResult(): String? {
        return dataResult
    }

}
