package com.wt.apkinfo.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.wt.apkinfo.base.BuildConfig
import com.wt.apkinfo.base.databinding.ActivityStartBinding
import com.wt.apkinfo.data.models.StartViewModel


class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private lateinit var viewModel: StartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.text1.text = BuildConfig.APP_VERSION_NAME

        viewModel = ViewModelProvider(this).get(StartViewModel::class.java)
        viewModel.data.observe(this) { intentData ->
            startActivity(
                Intent(applicationContext, MainActivity::class.java).setData(
                    intentData.data
                )
            )
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        
        runOnUiThread {
            viewModel.setData(intent)
        }
    }
}
