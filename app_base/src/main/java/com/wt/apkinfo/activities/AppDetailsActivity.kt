package com.wt.apkinfo.activities

import android.animation.Animator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.elevation.SurfaceColors
import com.wt.apkinfo.app.AppBuildType
import com.wt.apkinfo.base.BuildConfig
import com.wt.apkinfo.base.R
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.Prefs
import com.wt.apkinfo.data.images.ImageLoader
import com.wt.apkinfo.data.models.ApplicationDetailsViewModel
import com.wt.apkinfo.era.ERA
import com.wt.apkinfo.proto.DateTime
import com.wt.apkinfo.proto.PropertiesDialogAdapter
import com.wt.apkinfo.proto.Utils
import java.util.concurrent.Executors


class AppDetailsActivity : AppCompatActivity() {

    private val exec = Executors.newCachedThreadPool()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_app_details)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            v.setPadding(
                insets.getInsets(WindowInsetsCompat.Type.statusBars()).left,
                insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).right,
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            )
            insets
        }

        findViewById<MaterialCardView>(R.id.actionsCard)?.apply {
            setCardBackgroundColor(SurfaceColors.SURFACE_1.getColor(context))
        }

        val pkg = intent.getStringExtra("pkg")
        val model = ViewModelProvider(this).get(
            ApplicationDetailsViewModel::class.java
        )
        model.getData().observe(this) { data ->
            //val longPress = View.OnLongClickListener {
            //    Toast.makeText(this, it.contentDescription, Toast.LENGTH_LONG).show()
            //    false
            //}

            findViewById<TextView>(R.id.appName).text = data.name
            findViewById<TextView>(R.id.appPackage).text = data.pkg
            findViewById<TextView>(R.id.appVersionName).text = data.versionName
            findViewById<TextView>(R.id.appVersionCode).text = data.versionCode.toString()
            findViewById<TextView>(R.id.appSdkInfo).text = resources.getString(R.string.details_sdk, data.sdkMin, data.sdkTarget)
            //findViewById<TextView>(R.id.appSignature).text = data.signature
            findViewById<TextView>(R.id.appTime).text = resources.getString(
                R.string.details_time, DateTime.formatFull(data.timeInstall), DateTime.formatFull(
                    data.timeUpdate
                )
            )
            findViewById<TextView>(R.id.appInstallerPackage).text = data.installerPackage
            when {
                data.isDebuggable -> {
                    findViewById<TextView>(R.id.appTypeInfo).apply {
                        visibility = View.VISIBLE
                        setText(R.string.app_type_debug)
                    }
                }
                data.isSystemApp -> {
                    findViewById<TextView>(R.id.appTypeInfo).apply {
                        visibility = View.VISIBLE
                        setText(R.string.app_type_system)
                    }

                }
                else -> {
                    findViewById<TextView>(R.id.appTypeInfo).visibility = View.GONE
                }
            }
            if (Utils.isTV(this)) {
                findViewById<AppCompatTextView>(R.id.actionRun).visibility = if (data.launcherIntentTv == null) View.GONE else View.VISIBLE
            } else {
                findViewById<AppCompatTextView>(R.id.actionRun).visibility = if (data.launcherIntent == null) View.GONE else View.VISIBLE
            }
            findViewById<ImageView>(R.id.appLogo).apply {
                ImageLoader.get(this.context).load(data.icon, this)
            }

            //actionCopy.setOnLongClickListener(longPress)
            //actionInfo.setOnLongClickListener(longPress)
            //actionRun.setOnLongClickListener(longPress)

            if (BuildConfig.BUILD_FOR_MARKET == AppBuildType.APK) {
                findViewById<View>(R.id.actionShare).apply {
                    //setOnLongClickListener(longPress)
                    setOnClickListener {
                        startActivity(Intent().apply {
                            component = ComponentName(packageName, "$packageName.activities.ApkListActivity")
                            putExtra("pkg", data.pkg)
                            putExtra("version_name", data.versionName)
                            putExtra("version_code", data.versionCode)
                        })

                    }
                }
            } else {
                findViewById<View>(R.id.actionShare).visibility = View.GONE
            }

            findViewById<Toolbar>(R.id.toolbar)?.let { toolbar ->
                toolbar.menu.findItem(1).setOnMenuItemClickListener {
                    copyToClipboard(
                        "Name: ${data.name.toString()}\nPackage: ${data.pkg.toString()}\nSignature: ${data.signature.toString()}\n" +
                            "Version name: ${data.versionName.toString()}\n Version Code: ${data.versionCode}"
                    )
                    Toast.makeText(applicationContext, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
                    true
                }
                if (Utils.isTV(toolbar.context) && BuildConfig.BUILD_FOR_MARKET != AppBuildType.APK) {
                    toolbar.visibility = View.GONE
                }
            }

            if (BuildConfig.BUILD_FOR_MARKET == AppBuildType.APK) {
                findViewById<Toolbar>(R.id.toolbar).menu.findItem(2).setOnMenuItemClickListener {
                    startActivity(Intent().apply {
                        component = ComponentName(packageName, "$packageName.activities.ApkListActivity")
                        putExtra("pkg", data.pkg)
                        putExtra("version_name", data.versionName)
                        putExtra("version_code", data.versionCode)
                    })
                    true
                }
            }

            findViewById<View>(R.id.actionInfo).setOnClickListener {
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setData(Uri.parse("package:${data.pkg}"))
                    startActivity(intent)
                } catch (e: Exception) {
                    ERA.logException(e)
                    try {
                        startActivity(Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
                    } catch (w2: Exception) {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.app_run_error, e.message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            findViewById<View>(R.id.actionRun).setOnClickListener { ar ->
                if (Utils.isTV(ar.context)) {
                    data.launcherIntentTv?.let {
                        ar.visibility = View.VISIBLE
                        try {
                            startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (e: Exception) {
                            ERA.logException(e)
                            Toast.makeText(
                                this,
                                resources.getString(R.string.app_run_error, e.message),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } ?: run {
                        ar.visibility = View.GONE
                    }
                } else {
                    data.launcherIntent?.let {
                        ar.visibility = View.VISIBLE
                        try {
                            startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (e: Exception) {
                            ERA.logException(e)
                            Toast.makeText(
                                this,
                                resources.getString(R.string.app_run_error, e.message),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } ?: run {
                        ar.visibility = View.GONE
                    }
                }
            }
            data.meta.let {
                val title = resources.getString(R.string.details_metadata, it.size)
                findViewById<AppCompatTextView>(R.id.moreMeta).apply {
                    text = title
                    visibility = if (it.size > 0) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    setOnClickListener { _ ->
                        MaterialDialog(context).show {
                            title(0, title)
                            customListAdapter(PropertiesDialogAdapter(it))
                        }
                    }
                    if (Utils.isTV(this.context)) {
                        visibility = View.GONE
                    }
                }

            }
            data.activities.let {
                val title = resources.getString(R.string.details_activities, it.size)
                findViewById<AppCompatTextView>(R.id.moreActivities).apply {
                    text = title
                    visibility = if (it.size > 0) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    setOnClickListener { _ ->
                        MaterialDialog(context).show {
                            title(0, title)
                            customListAdapter(PropertiesDialogAdapter(it))
                        }
                    }
                    if (Utils.isTV(this.context)) {
                        visibility = View.GONE
                    }
                }
            }
            data.services.let {
                val title = resources.getString(R.string.details_services, it.size)
                findViewById<AppCompatTextView>(R.id.moreServices).apply {
                    text = title
                    visibility = if (it.size > 0) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    setOnClickListener { _ ->
                        MaterialDialog(context).show {
                            title(0, title)
                            customListAdapter(PropertiesDialogAdapter(it))
                        }
                    }

                }
            }
            data.providers.let {
                val title = resources.getString(R.string.details_providers, it.size)
                findViewById<AppCompatTextView>(R.id.moreProviders).apply {
                    text = title
                    visibility = if (it.size > 0) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    setOnClickListener { _ ->
                        MaterialDialog(context).show {
                            title(0, title)
                            customListAdapter(PropertiesDialogAdapter(it))
                        }
                    }
                }
            }
            data.receivers.let {
                val title = resources.getString(R.string.details_receivers, it.size)
                findViewById<AppCompatTextView>(R.id.moreReceivers).apply {
                    text = title
                    visibility = if (it.size > 0) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    setOnClickListener { _ ->
                        MaterialDialog(context).show {
                            title(0, title)
                            customListAdapter(PropertiesDialogAdapter(it))
                        }
                    }
                }
            }
            /*data.directories.let {
                val title = resources.getString(R.string.details_directories, it.size)
                findViewById<AppCompatTextView>(R.id.moreDirectories).apply {
                    text = title
                    visibility = if (it.size > 0) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    setOnClickListener { _ ->
                        MaterialDialog(context).show {
                            title(0, title)
                            customListAdapter(PropertiesDialogAdapter(it))
                        }
                    }
                }
            }
             */
            data.permissions.let {
                val title = resources.getString(R.string.details_permissions, it.size)
                findViewById<AppCompatTextView>(R.id.morePermissions).apply {
                    text = title
                    visibility = if (it.size > 0) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    setOnClickListener { _ ->
                        MaterialDialog(context).show {
                            title(0, title)
                            customListAdapter(PropertiesDialogAdapter(it))
                        }
                    }
                }
            }
            /*
            data.sharedLibraries.let {
                val title = resources.getString(R.string.details_shared_libraries, it.size)
                findViewById<AppCompatTextView>(R.id.moreSharedLibraries).apply {
                    text = title
                    visibility = if (it.size > 0) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    setOnClickListener { _ ->
                        MaterialDialog(context).show {
                            title(0, title)
                            customListAdapter(PropertiesDialogAdapter(it))
                        }
                    }
                }
            }

            data.nativeLibraries.let {
                val title = resources.getString(R.string.details_native_libraries, it.size)
                findViewById<AppCompatTextView>(R.id.moreNativeLibraries).apply {
                    text = title
                    visibility = if (it.size > 0) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    setOnClickListener { _ ->
                        MaterialDialog(context).show {
                            title(0, title)
                            customListAdapter(PropertiesDialogAdapter(it))
                        }
                    }
                }
            }
             */
            findViewById<View>(R.id.moreOtherProperties).setOnClickListener {
                val props = ArrayList<String>()
                props.add(resources.getString(R.string.details_property_large_heap) + "\n" + data.isLargeHeap.toString())
                props.add(resources.getString(R.string.details_property_hw_accelerated) + "\n" + data.isHwAccelerated.toString())
                props.add(resources.getString(R.string.details_property_rtl_supported) + "\n" + data.isSupportRtl.toString())
                MaterialDialog(this).show {
                    title(R.string.details_other_properties)
                    customListAdapter(PropertiesDialogAdapter(props))
                }
            }
            findViewById<View>(R.id.loader).apply {
                val loader = this
                alpha = 1f
                animate()
                    .setDuration(250)
                    .alpha(0f)
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator) {}
                        override fun onAnimationEnd(animation: Animator) {
                            loader.visibility = View.GONE
                        }
                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationStart(animation: Animator) {}
                    })
                    .start()
            }

            hideTvInterface()
        }
        pkg?.let {
            model.fetchInfo(pkg)
        } ?: run {
            // no package name
            finish()
        }

        findViewById<View>(R.id.actionUninstall)?.apply {
            setOnClickListener{
                try {
                    val intent = Intent(Intent.ACTION_DELETE)
                    intent.data = Uri.parse("package:$pkg")
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        it.context,
                        resources.getString(R.string.app_run_error, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        findViewById<View>(R.id.actionInfo)?.apply {
            setOnClickListener{
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setData(Uri.parse("package:${pkg}"))
                    startActivity(intent)
                } catch (e: Exception) {
                    ERA.logException(e)
                    try {
                        startActivity(Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
                    } catch (w2: Exception) {
                        Toast.makeText(
                            it.context,
                            resources.getString(R.string.app_run_error, e.message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        findViewById<Toolbar>(R.id.toolbar).apply {
            setTitle(R.string.app_details)
            setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            setNavigationOnClickListener {
                finish()
            }
            navigationContentDescription = resources.getString(R.string.back)
            menu.apply {
                add(R.string.find_in_market).setOnMenuItemClickListener {
                    if (AppBuildType.HUAWEI == BuildConfig.BUILD_FOR_MARKET) {
                        val hwAppId = "101754683"
                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("appmarket://details?id=$pkg")
                                )
                            )
                        } catch (e1: java.lang.Exception) {
                            try {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://appgallery.cloud.huawei.com/marketshare/app/C$hwAppId")
                                    )
                                )
                            } catch (e2: java.lang.Exception) {
                                ERA.logException(java.lang.Exception("No App Gallery and WebBrowser"))
                            }
                        }
                    } else {
                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$pkg")
                                ).setPackage("com.android.vending")
                            )
                        } catch (e: java.lang.Exception) {
                            try {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                                    )
                                )
                            } catch (e2: java.lang.Exception) {
                                ERA.logException(java.lang.Exception("No Play Store app and WebBrowser"))
                            }
                        }
                    }
                    true
                }
                add(0, 1, 0, R.string.copy)
                if (BuildConfig.BUILD_FOR_MARKET == AppBuildType.APK) {
                    add(0, 2, 0, R.string.share)
                }
            }
        }


        if (savedInstanceState == null) {
            pkg?.let {
                logDetailsOpenCounter(it)
            }
        }

        hideTvInterface()
    }

    private fun hideTvInterface() {
        if (Utils.isTV(this) && BuildConfig.BUILD_FOR_MARKET != AppBuildType.APK) {
            findViewById<View>(R.id.moreInfoHeader).visibility = View.GONE
            findViewById<View>(R.id.moreMeta).visibility = View.GONE
            findViewById<View>(R.id.morePermissions).visibility = View.GONE
            findViewById<View>(R.id.moreActivities).visibility = View.GONE
            findViewById<View>(R.id.moreServices).visibility = View.GONE
            findViewById<View>(R.id.moreProviders).visibility = View.GONE
            findViewById<View>(R.id.moreReceivers).visibility = View.GONE
            findViewById<View>(R.id.moreDirectories).visibility = View.GONE
            findViewById<View>(R.id.moreSharedLibraries).visibility = View.GONE
            findViewById<View>(R.id.moreNativeLibraries).visibility = View.GONE
            findViewById<View>(R.id.moreOtherProperties).visibility = View.GONE
        }
    }

    private fun copyToClipboard(text: String) {
        //exec.execute {
            val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ApkInfo", text)
            clipboard.setPrimaryClip(clip)
        //}
    }

    private fun logDetailsOpenCounter(pkg: String) {
        try {
            val value = Prefs(applicationContext).appDetailsOpenCounter
            if (BuildConfig.DEBUG) {
                Log.i("AppDetailsActivity", "logDetailsOpenCounter: pkg=$pkg, counter=$value")
            }
        } catch (e: Exception) {
            ERA.logException(e)
        }
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
