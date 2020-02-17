package com.wt.apkinfo.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.wt.apkinfo.BuildConfig
import com.wt.apkinfo.R
import com.wt.apkinfo.data.ApplicationDetailsInfo
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.models.ApplicationDetailsViewModel
import kotlinx.android.synthetic.main.activity_app_details.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class AppDetailsActivity : AppCompatActivity() {

    //private var toolbar: MaterialToolbar? = null
    //private var appLogo: ImageView? = null
    //private var appInfo: ApplicationEntryInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.activity_app_details)

        val model = ViewModelProvider(this).get<ApplicationDetailsViewModel>(ApplicationDetailsViewModel::class.java)
        model.getData().observe(this, Observer<ApplicationDetailsInfo> { data ->
            appName.text = data.name
            appPackage.text = data.pkg
            appVersionName.text = data.versionName
            appVersionCode.text = data.versionCode.toString()
        })
        model.fetchInfo("com.hv.replaio.beta")

        if (BuildConfig.DEBUG) {
            //appInfo = ApplicationEntryInfo(0, "com.app.test", "Sample App Name", null)

        }

        //appLogo.contentDescription = "";

        toolbar.setTitle(R.string.app_details)
        //appName.text = appInfo?.name
        //appPackage.text = appInfo?.pkg
        //appVersionName.text = "1.2.3"
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    companion object {
        @JvmStatic
        fun show(ctx: Context, appInfo: ApplicationEntryInfo?)  {
            ctx.startActivity(Intent(ctx, AppDetailsActivity::class.java))
        }
    }
}
