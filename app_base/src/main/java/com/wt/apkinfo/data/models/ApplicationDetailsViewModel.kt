package com.wt.apkinfo.data.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wt.apkinfo.data.ApplicationDetailsInfo
import com.wt.apkinfo.data.repositories.ApplicationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApplicationDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val data = MutableLiveData<ApplicationDetailsInfo>()
    private val appRepository: ApplicationsRepository = ApplicationsRepository(application)

    fun getData() : MutableLiveData<ApplicationDetailsInfo> {
        return data;
    }

    fun fetchInfo(pkgName: String) {
        viewModelScope.launch(Dispatchers.IO) { data.postValue(appRepository.getApplicationDetailsInfo(pkgName)) }
    }

}
