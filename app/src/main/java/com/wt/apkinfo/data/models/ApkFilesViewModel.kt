package com.wt.apkinfo.data.models

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.wt.apkinfo.data.ApkFileEntryInfo
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.repositories.ApplicationsRepository
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import androidx.lifecycle.MutableLiveData as MutableLiveData1

class ApkFilesViewModel(application: Application) : AndroidViewModel(application) {

    private val data = MutableLiveData1<List<ApkFileEntryInfo>>()
    private val exec = Executors.newCachedThreadPool()
    private val appRepository: ApplicationsRepository = ApplicationsRepository(application)

    fun getData() : MutableLiveData1<List<ApkFileEntryInfo>> {
        return data;
    }

    fun list(pkgName: String) {
        exec.execute { data.postValue(appRepository.getApkFiles(pkgName)) }
    }

}