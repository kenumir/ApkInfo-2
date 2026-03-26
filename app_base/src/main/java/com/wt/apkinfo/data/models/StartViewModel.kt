package com.wt.apkinfo.data.models

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class StartViewModel(application: Application) : AndroidViewModel(application) {

    private val _data = MutableLiveData<Intent>()
    val data: LiveData<Intent> = _data

    fun setData(intent: Intent) {
        _data.value = intent
    }
}
