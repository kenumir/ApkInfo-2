package com.wt.apkinfo.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.wt.apkinfo.BuildConfig
import com.wt.apkinfo.R
import com.wt.apkinfo.data.models.StartViewModel
import kotlinx.android.synthetic.main.activity_start.*


class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        text1.text = BuildConfig.VERSION_NAME

        val model = ViewModelProvider(this).get<StartViewModel>(StartViewModel::class.java)
        model.getData()
            .observe(this, Observer<Intent> { intentData ->
                startActivity(Intent(applicationContext, MainActivity::class.java).setData(intentData.data))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            })
        runOnUiThread {
            model.getData().value = intent
        }
    }
}
