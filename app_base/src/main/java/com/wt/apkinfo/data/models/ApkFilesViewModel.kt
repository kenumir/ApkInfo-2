package com.wt.apkinfo.data.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wt.apkinfo.data.ApkFileEntryInfo
import com.wt.apkinfo.data.repositories.ApplicationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class ApkFilesUiState {
    object Loading : ApkFilesUiState()
    data class Success(val items: List<ApkFileEntryInfo>) : ApkFilesUiState()
    data class Error(val message: String) : ApkFilesUiState()
}

class ApkFilesViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData<ApkFilesUiState>(ApkFilesUiState.Loading)
    val uiState: LiveData<ApkFilesUiState> = _uiState

    private val appRepository = ApplicationsRepository(application)

    fun list(pkgName: String) {
        _uiState.value = ApkFilesUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val files = appRepository.getApkFiles(pkgName)
                _uiState.postValue(ApkFilesUiState.Success(files))
            } catch (e: Exception) {
                _uiState.postValue(ApkFilesUiState.Error(e.message ?: "Unknown error"))
            }
        }
    }
}
