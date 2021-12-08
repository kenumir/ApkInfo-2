package com.wt.apkinfo.data.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.wt.apkinfo.data.ApkFileEntryInfo
import com.wt.apkinfo.data.repositories.ApplicationsRepository
import java.util.concurrent.Executors
import androidx.lifecycle.MutableLiveData as MutableLiveData1

class ApkFilesViewModel(application: Application) : AndroidViewModel(application) {

    private val data = MutableLiveData1<List<ApkFileEntryInfo>>()
    private val exec = Executors.newCachedThreadPool()
    private val appRepository: ApplicationsRepository = ApplicationsRepository(application)
    private var lastData: List<ApkFileEntryInfo>? = null

    fun getData() : MutableLiveData1<List<ApkFileEntryInfo>> {
        return data;
    }

    fun list(pkgName: String) {
        exec.execute {
            lastData = appRepository.getApkFiles(pkgName).also {
                data.postValue(it)
            }
        }
    }

    fun getLastData(): List<ApkFileEntryInfo>? {
        return lastData
    }

}