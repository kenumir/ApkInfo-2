package com.wt.apkinfo.data.models

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.wt.apkinfo.App
import com.wt.apkinfo.BuildConfig
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.Prefs
import com.wt.apkinfo.data.repositories.ApplicationsRepository
import com.wt.apkinfo.era.ERA
import com.wt.apkinfo.proto.ListSortOrder
import java.util.concurrent.Executors
import androidx.lifecycle.MutableLiveData as MutableLiveData1

class ApplicationsViewModel(application: Application) : AndroidViewModel(application) {

    private val data = MutableLiveData1<List<ApplicationEntryInfo?>>()
    private val exec = Executors.newCachedThreadPool()
    private val appRepository: ApplicationsRepository = ApplicationsRepository(application)

    private var packageReceiver: BroadcastReceiver? = null
    private var lastSearchQuery: String? = null
    private var showAllApps = Prefs(application).allApps == 1
    private var sortOrder: ListSortOrder = Prefs(application).listSortOrder

    init {
        exec.execute { data.postValue(appRepository.getAppList(null, showAllApps, sortOrder)) }
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
                exec.execute { data.postValue(appRepository.getAppList(lastSearchQuery, showAllApps, sortOrder)) }
            }
        }
        application.registerReceiver(packageReceiver, intentFilter)

    }

    fun getData() : MutableLiveData1<List<ApplicationEntryInfo?>> {
        return data
    }

    fun search(query: String?) {
        lastSearchQuery = query
        data.value = ArrayList(listOf(null))
        exec.execute { data.postValue(appRepository.getAppList(query, showAllApps, sortOrder)) }

        try {
            query?.let {
                if (it.isNotEmpty()) {
                    FirebaseAnalytics
                        .getInstance(getApplication())
                        .logEvent(FirebaseAnalytics.Event.SEARCH, Bundle().apply {
                            putString(FirebaseAnalytics.Param.SEARCH_TERM, it)
                        })
                }
            }
        } catch (e: Exception) {
            ERA.logException(e)
        }
    }

    fun showAllApps(b: Boolean) {
        showAllApps = b
        data.value = ArrayList(listOf(null))
        exec.execute { data.postValue(appRepository.getAppList(lastSearchQuery, showAllApps, sortOrder)) }
    }

    fun showSortOrder(b: ListSortOrder) {
        sortOrder = b
        data.value = ArrayList(listOf(null))
        exec.execute { data.postValue(appRepository.getAppList(lastSearchQuery, showAllApps, sortOrder)) }
    }

    override fun onCleared() {
        getApplication<App>().unregisterReceiver(packageReceiver)
    }
}