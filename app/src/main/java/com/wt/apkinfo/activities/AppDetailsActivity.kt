package com.wt.apkinfo.activities

import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.wt.apkinfo.R
import com.wt.apkinfo.data.ApplicationDetailsInfo
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.models.ApplicationDetailsViewModel
import com.wt.apkinfo.proto.DateTime
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

        val pkg = intent.getStringExtra("pkg")
        val model = ViewModelProvider(this).get<ApplicationDetailsViewModel>(ApplicationDetailsViewModel::class.java)
        model.getData().observe(this, Observer<ApplicationDetailsInfo> { data ->
            appName.text = data.name
            appPackage.text = data.pkg
            appVersionName.text = data.versionName
            appVersionCode.text = data.versionCode.toString()
            appLogo.setImageDrawable(data.icon)
            appSdkInfo.text = "Min: ${data.sdkMin}, Target: ${data.sdkTarget}"
            appSignature.text = data.signature
            appTime.text = "Install: " + DateTime.formatFull(data.timeInstall) +
                    "\nUpdate: " + DateTime.formatFull(data.timeUpdate)
            appInstallerPackage.text = data.installerPackage
            actionCopy.setOnClickListener {
                copyToClipboard(
                    "Name: ${data.name.toString()}\nPackage: ${data.pkg.toString()}\nSignature: ${data.signature.toString()}\n" +
                            "Version name: ${data.versionName.toString()}\n Version Code: ${data.versionCode}"
                )
                Toast.makeText(applicationContext, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
            }
        })
        pkg?.let {
            model.fetchInfo(pkg)
        } ?: run {
            // no package name
        }

        toolbar.setTitle(R.string.app_details)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    private fun copyToClipboard(text: String) {
        val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ApkInfo", text)
        clipboard.setPrimaryClip(clip)
    }

    companion object {
        @JvmStatic
        fun show(ctx: Context, appInfo: ApplicationEntryInfo?)  {
            val it = Intent(ctx, AppDetailsActivity::class.java)
            it.putExtra("pkg", appInfo?.pkg)
            ctx.startActivity(it)
        }
    }
}
