package com.wt.apkinfo.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.wt.apkinfo.base.BuildConfig
import com.wt.apkinfo.base.databinding.ActivityStartBinding
import com.wt.apkinfo.data.models.StartViewModel
import com.wt.apkinfo.proto.applyFadeTransition


class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private val viewModel: StartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.text1.text = BuildConfig.APP_VERSION_NAME

        viewModel.data.observe(this) { intentData ->
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                data = intentData.data
            }
            startActivity(mainIntent)
            finish()
            applyFadeTransition()
        }
        
        viewModel.setData(intent)
    }
}
