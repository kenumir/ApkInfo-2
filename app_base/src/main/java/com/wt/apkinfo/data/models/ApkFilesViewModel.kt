package com.wt.apkinfo.data.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wt.apkinfo.data.ApkFileEntryInfo
import com.wt.apkinfo.data.repositories.ApplicationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApkFilesViewModel(application: Application) : AndroidViewModel(application) {

    private val data = MutableLiveData<List<ApkFileEntryInfo>>()
    private val appRepository: ApplicationsRepository = ApplicationsRepository(application)
    private var lastData: List<ApkFileEntryInfo>? = null

    fun getData() : MutableLiveData<List<ApkFileEntryInfo>> {
        return data;
    }

    fun list(pkgName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            lastData = appRepository.getApkFiles(pkgName).also {
                data.postValue(it)
            }
        }
    }

    fun getLastData(): List<ApkFileEntryInfo>? {
        return lastData
    }

}
