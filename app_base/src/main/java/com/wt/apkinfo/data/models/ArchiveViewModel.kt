package com.wt.apkinfo.data.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wt.apkinfo.data.repositories.ApplicationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

sealed class ArchiveUiState {
    object Idle : ArchiveUiState()
    object Loading : ArchiveUiState()
    data class Success(val file: File) : ArchiveUiState()
    data class Error(val message: String) : ArchiveUiState()
}

class ArchiveViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData<ArchiveUiState>(ArchiveUiState.Idle)
    val uiState: LiveData<ArchiveUiState> = _uiState

    private val appRepository = ApplicationsRepository(application)

    fun startMakeArchive(pkg: String, versionName: String?, versionCode: Int) {
        _uiState.value = ArchiveUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = appRepository.createZipArchive(pkg, versionName, versionCode)
                _uiState.postValue(ArchiveUiState.Success(file))
            } catch (e: Exception) {
                _uiState.postValue(ArchiveUiState.Error(e.message ?: "Failed to create archive"))
            }
        }
    }
    
    fun resetState() {
        _uiState.value = ArchiveUiState.Idle
    }
}
