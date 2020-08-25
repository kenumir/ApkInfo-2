package com.wt.apkinfo.data.models

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.wt.apkinfo.BuildConfig
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.repositories.ApplicationsRepository
import java.util.concurrent.Executors
import androidx.lifecycle.MutableLiveData as MutableLiveData1

class ApplicationsViewModel(application: Application) : AndroidViewModel(application) {

    private val data = MutableLiveData1<List<ApplicationEntryInfo?>>()
    private val exec = Executors.newCachedThreadPool()
    private val appRepository: ApplicationsRepository = ApplicationsRepository(application)

    private var packageReceiver: BroadcastReceiver? = null
    private val context: Context = application.applicationContext
    private var lastSearchQuery: String? = null
    private var showAllApps = false

    init {
        exec.execute { data.postValue(appRepository.getAppList(null, showAllApps)) }
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
        intentFilter.addDataScheme("package")
        packageReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (BuildConfig.DEBUG) {
                    Log.i("tests", "ACTION_PACKAGE_: " + p1?.action)
                }
                exec.execute { data.postValue(appRepository.getAppList(lastSearchQuery, showAllApps)) }
            }
        }
        context.registerReceiver(packageReceiver, intentFilter)
    }

    fun getData() : MutableLiveData1<List<ApplicationEntryInfo?>> {
        return data;
    }

    fun search(query: String?) {
        lastSearchQuery = query
        data.value = ArrayList(listOf(null))
        exec.execute { data.postValue(appRepository.getAppList(query, showAllApps)) }
    }

    fun showAllApps(b: Boolean) {
        showAllApps = b
        data.value = ArrayList(listOf(null))
        exec.execute { data.postValue(appRepository.getAppList(lastSearchQuery, showAllApps)) }
    }

    override fun onCleared() {
        context.unregisterReceiver(packageReceiver)
    }
}