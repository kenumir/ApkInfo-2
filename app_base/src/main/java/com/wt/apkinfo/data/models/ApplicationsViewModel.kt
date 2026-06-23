package com.wt.apkinfo.data.models

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.Prefs
import com.wt.apkinfo.data.repositories.ApplicationsRepository
import com.wt.apkinfo.proto.FilterAppType
import com.wt.apkinfo.proto.FilterInstaller
import com.wt.apkinfo.proto.FilterTargetSdk
import com.wt.apkinfo.proto.ListSortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class ApplicationsUiState(
    val items: List<ApplicationEntryInfo?> = listOf(null), // Domyślnie loader
    val isLoading: Boolean = false,
    val filterAppType: FilterAppType = FilterAppType.ALL,
    val listSortOrder: ListSortOrder = ListSortOrder.NAME,
    val filterInstaller: FilterInstaller = FilterInstaller.ALL,
    val filterTargetSdk: FilterTargetSdk = FilterTargetSdk.ALL,
    val query: String = ""
)

class ApplicationsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData(ApplicationsUiState())
    val uiState: LiveData<ApplicationsUiState> = _uiState

    private val appRepository: ApplicationsRepository = ApplicationsRepository(application)
    private val prefs = Prefs(application)

    private var fetchJob: Job? = null
    private var packageReceiver: BroadcastReceiver? = null

    init {
        setupPackageReceiver()
        // Inicjalizacja stanu z preferencji
        _uiState.value = _uiState.value?.copy(
            filterAppType = prefs.listFilterAppType,
            listSortOrder = prefs.listSortOrder,
            filterInstaller = prefs.listFilterInstaller,
            filterTargetSdk = prefs.listFilterTargetSdk
        )
        refreshData()
    }

    private fun setupPackageReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addDataScheme("package")
        }
        packageReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                refreshData()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplication<Application>().registerReceiver(
                packageReceiver, 
                intentFilter, 
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            getApplication<Application>().registerReceiver(packageReceiver, intentFilter)
        }
    }

    fun refreshData() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value ?: ApplicationsUiState()
            
            // Ustawiamy stan ładowania (null na początku listy dla adaptera)
            if (currentState.items.isEmpty() || currentState.items[0] != null) {
                _uiState.postValue(currentState.copy(items = listOf(null), isLoading = true))
            }

            val results = appRepository.getAppList(
                currentState.query.ifEmpty { null },
                currentState.filterAppType,
                currentState.listSortOrder,
                currentState.filterInstaller,
                currentState.filterTargetSdk
            )
            
            _uiState.postValue(currentState.copy(
                items = results,
                isLoading = false
            ))
        }
    }

    fun search(query: String) {
        val currentState = _uiState.value ?: ApplicationsUiState()
        if (currentState.query == query) return
        
        _uiState.value = currentState.copy(query = query, items = listOf(null))
        refreshData()
    }

    fun setAppType(t: FilterAppType) {
        prefs.listFilterAppType = t
        _uiState.value = _uiState.value?.copy(filterAppType = t, items = listOf(null))
        refreshData()
    }

    fun setAppInstaller(t: FilterInstaller) {
        prefs.listFilterInstaller = t
        _uiState.value = _uiState.value?.copy(filterInstaller = t, items = listOf(null))
        refreshData()
    }

    fun setTargetSdk(t: FilterTargetSdk) {
        prefs.listFilterTargetSdk = t
        _uiState.value = _uiState.value?.copy(filterTargetSdk = t, items = listOf(null))
        refreshData()
    }

    fun setSortOrder(order: ListSortOrder) {
        prefs.listSortOrder = order
        _uiState.value = _uiState.value?.copy(listSortOrder = order, items = listOf(null))
        refreshData()
    }

    override fun onCleared() {
        super.onCleared()
        packageReceiver?.let {
            getApplication<Application>().unregisterReceiver(it)
        }
    }
}
