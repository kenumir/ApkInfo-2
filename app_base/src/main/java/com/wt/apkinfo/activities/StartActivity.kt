package com.wt.apkinfo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.wt.apkinfo.base.BuildConfig
import com.wt.apkinfo.base.R
import com.wt.apkinfo.data.models.StartViewModel


class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        findViewById<TextView>(R.id.text1).text = BuildConfig.APP_VERSION_NAME

        val model = ViewModelProvider(this).get(StartViewModel::class.java)
        model.getData()
            .observe(this) { intentData ->
                startActivity(
                    Intent(applicationContext, MainActivity::class.java).setData(
                        intentData.data
                    )
                )
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        runOnUiThread {
            model.getData().value = intent
        }
    }
}
