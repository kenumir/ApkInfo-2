package com.wt.apkinfo.data.models

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wt.apkinfo.App
import com.wt.apkinfo.base.BuildConfig
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.Prefs
import com.wt.apkinfo.data.repositories.ApplicationsRepository
import com.wt.apkinfo.proto.FilterAppType
import com.wt.apkinfo.proto.FilterInstaller
import com.wt.apkinfo.proto.FilterTargetSdk
import com.wt.apkinfo.proto.ListSortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApplicationsViewModel(application: Application) : AndroidViewModel(application) {

    private val data = MutableLiveData<List<ApplicationEntryInfo?>>()
    private val appRepository: ApplicationsRepository = ApplicationsRepository(application)

    private var packageReceiver: BroadcastReceiver? = null
    private var lastSearchQuery: String? = null
    private var appTypeParam: FilterAppType
        get() = Prefs(getApplication()).listFilterAppType
        set(v) {
            Prefs(getApplication()).listFilterAppType = v
        }
    private var sortOrderParam: ListSortOrder
        get() = Prefs(getApplication()).listSortOrder
        set(v) {
            Prefs(getApplication()).listSortOrder = v
        }
    private var installerParam: FilterInstaller
        get() = Prefs(getApplication()).listFilterInstaller
        set(v) {
            Prefs(getApplication()).listFilterInstaller = v
        }
    private var targetSdkParam: FilterTargetSdk
        get() = Prefs(getApplication()).listFilterTargetSdk
        set(v) {
            Prefs(getApplication()).listFilterTargetSdk = v
        }

    var filterAppType: FilterAppType? = null
    var listSortOrder: ListSortOrder? = null
    var filterInstaller: FilterInstaller? = null
    var filterTargetSdk: FilterTargetSdk? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            assignValues()
            data.postValue(appRepository.getAppList(null, appTypeParam, sortOrderParam, installerParam, targetSdkParam))
        }
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
                viewModelScope.launch(Dispatchers.IO) {
                    assignValues()
                    data.postValue(appRepository.getAppList(lastSearchQuery, appTypeParam, sortOrderParam, installerParam, targetSdkParam))
                }
            }
        }
        application.registerReceiver(packageReceiver, intentFilter)
    }

    private fun assignValues() {
        filterAppType = appTypeParam
        listSortOrder = sortOrderParam
        filterInstaller = installerParam
        filterTargetSdk = targetSdkParam
    }

    fun getData() : MutableLiveData<List<ApplicationEntryInfo?>> {
        return data
    }

    fun search(query: String?) {
        lastSearchQuery = query
        data.value = ArrayList(listOf(null))
        viewModelScope.launch(Dispatchers.IO) {
            assignValues()
            data.postValue(appRepository.getAppList(query, appTypeParam, sortOrderParam, installerParam, targetSdkParam))
        }
    }

    fun setAppType(t: FilterAppType) {
        appTypeParam = t
        data.value = ArrayList(listOf(null))
        viewModelScope.launch(Dispatchers.IO) {
            assignValues()
            data.postValue(appRepository.getAppList(lastSearchQuery, appTypeParam, sortOrderParam, installerParam, targetSdkParam))
        }
    }

    fun setAppInstaller(t: FilterInstaller) {
        installerParam = t
        data.value = ArrayList(listOf(null))
        viewModelScope.launch(Dispatchers.IO) {
            assignValues()
            data.postValue(appRepository.getAppList(lastSearchQuery, appTypeParam, sortOrderParam, installerParam, targetSdkParam))
        }
    }

    fun setTargetSdk(t: FilterTargetSdk) {
        targetSdkParam = t
        data.value = ArrayList(listOf(null))
        viewModelScope.launch(Dispatchers.IO) {
            assignValues()
            data.postValue(appRepository.getAppList(lastSearchQuery, appTypeParam, sortOrderParam, installerParam, targetSdkParam))
        }
    }

    fun showSortOrder(b: ListSortOrder) {
        sortOrderParam = b
        data.value = ArrayList(listOf(null))
        viewModelScope.launch(Dispatchers.IO) {
            assignValues()
            data.postValue(appRepository.getAppList(lastSearchQuery, appTypeParam, sortOrderParam, installerParam, targetSdkParam))
        }
    }

    override fun onCleared() {
        getApplication<App>().unregisterReceiver(packageReceiver)
    }
}
