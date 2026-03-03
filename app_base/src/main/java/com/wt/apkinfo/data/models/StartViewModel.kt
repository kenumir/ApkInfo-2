package com.wt.apkinfo.data.models

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class StartViewModel(application: Application) : AndroidViewModel(application) {

    private val data = MutableLiveData<Intent>()

    fun getData() : MutableLiveData<Intent> {
        return data;
    }
}
