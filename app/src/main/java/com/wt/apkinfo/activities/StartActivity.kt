package com.wt.apkinfo.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.wt.apkinfo.data.models.StartViewModel


class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model = ViewModelProvider(this).get<StartViewModel>(StartViewModel::class.java)
        model.getData()
            .observe(this, Observer<Intent> { intentData ->
                startActivity(Intent(applicationContext, MainActivity::class.java).setData(intentData.data))
                finish()
                overridePendingTransition(0, 0)
            })
        model.getData().value = intent
    }
}
