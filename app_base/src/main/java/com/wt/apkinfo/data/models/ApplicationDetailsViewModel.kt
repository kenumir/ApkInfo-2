package com.wt.apkinfo.data.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wt.apkinfo.data.ApplicationDetailsInfo
import com.wt.apkinfo.data.repositories.ApplicationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class DetailsUiState {
    object Loading : DetailsUiState()
    data class Success(val data: ApplicationDetailsInfo) : DetailsUiState()
    data class Error(val message: String) : DetailsUiState()
}

class ApplicationDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData<DetailsUiState>(DetailsUiState.Loading)
    val uiState: LiveData<DetailsUiState> = _uiState

    private val appRepository: ApplicationsRepository = ApplicationsRepository(application)

    fun fetchInfo(pkgName: String) {
        _uiState.value = DetailsUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val details = appRepository.getApplicationDetailsInfo(pkgName)
                _uiState.postValue(DetailsUiState.Success(details))
            } catch (e: Exception) {
                _uiState.postValue(DetailsUiState.Error(e.message ?: "Unknown error"))
            }
        }
    }
}
