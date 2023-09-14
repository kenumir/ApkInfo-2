package com.wt.apkinfo.data.models

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData as MutableLiveData1

class StartViewModel(application: Application) : AndroidViewModel(application) {

    private val data = MutableLiveData1<Intent>()

    fun getData() : MutableLiveData1<Intent> {
        return data;
    }
}