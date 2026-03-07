package com.wt.apkinfo.activities

import android.os.Bundle
import android.os.RemoteException
import android.text.TextUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.wt.apkinfo.App
import com.wt.apkinfo.base.databinding.ActivityMainBinding
import com.wt.apkinfo.era.ERA
import com.wt.apkinfo.fragments.AppListFragment

class MainActivity : AppCompatActivity(), InstallReferrerStateListener {

    private var mReferrerClient: InstallReferrerClient? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            insets
        }

        if (savedInstanceState == null) {
            val ir = (application as App).getUserInfo().installReferrer
            if (TextUtils.isEmpty(ir)) {
                mReferrerClient = InstallReferrerClient.newBuilder(this).build()
                try {
                    mReferrerClient?.startConnection(this)
                } catch (e: Exception) {
                    ERA.logException(e)
                }
            }
        }
    }

    override fun onBackPressed() {
        supportFragmentManager.findFragmentByTag("AppListFragment_Tag")?.let {
            if (it is AppListFragment) {
                if (it.onBackAction()) {
                    return
                }
            }
        }
        super.onBackPressed()
    }

    override fun onInstallReferrerSetupFinished(responseCode: Int) {
        if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
            try {
                mReferrerClient?.let {
                    val response = it.installReferrer
                    val ir = response.installReferrer
                    (application as App).getUserInfo().saveInstallReferrer(ir)
                    it.endConnection()
                }
            } catch (e: RemoteException) {
                ERA.logException(e)
            }
        }
    }

    override fun onInstallReferrerServiceDisconnected() {

    }
}
