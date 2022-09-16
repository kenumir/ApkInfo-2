package com.wt.apkinfo.activities

import android.animation.Animator
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.google.firebase.analytics.FirebaseAnalytics
import com.wt.apkinfo.BuildConfig
import com.wt.apkinfo.R
import com.wt.apkinfo.app.AppBuildType
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.Prefs
import com.wt.apkinfo.data.images.ImageLoader
import com.wt.apkinfo.data.models.ApplicationDetailsViewModel
import com.wt.apkinfo.era.ERA
import com.wt.apkinfo.proto.DateTime
import com.wt.apkinfo.proto.PropertiesDialogAdapter
import com.wt.userinfo.UserInfo
import kotlinx.android.synthetic.main.activity_app_details.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.util.concurrent.Executors


class AppDetailsActivity : AppCompatActivity() {

    private val exec = Executors.newCachedThreadPool()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_details)

        val pkg = intent.getStringExtra("pkg")
        val model = ViewModelProvider(this).get(
            ApplicationDetailsViewModel::class.java
        )
        model.getData().observe(this) { data ->
            val longPress = View.OnLongClickListener {
                Toast.makeText(this, it.contentDescription, Toast.LENGTH_LONG).show()
                false
            }

            appName.text = data.name
            appPackage.text = data.pkg
            appVersionName.text = data.versionName
            appVersionCode.text = data.versionCode.toString()
            appSdkInfo.text = resources.getString(R.string.details_sdk, data.sdkMin, data.sdkTarget)
            appSignature.text = data.signature
            appTime.text = resources.getString(
                R.string.details_time, DateTime.formatFull(data.timeInstall), DateTime.formatFull(
                    data.timeUpdate
                )
            )
            appInstallerPackage.text = data.installerPackage
            when {
                data.isDebuggable -> {
                    appTypeInfo.visibility = View.VISIBLE
                    appTypeInfo.setText(R.string.app_type_debug)
                }
                data.isSystemApp -> {
                    appTypeInfo.visibility = View.VISIBLE
                    appTypeInfo.setText(R.string.app_type_system)
                }
                else -> {
                    appTypeInfo.visibility = View.GONE
                }
            }
            actionRun.visibility = if (data.launcherIntent == null) View.GONE else View.VISIBLE
            ImageLoader.get(appLogo.context).load(data.icon, appLogo)

            actionCopy.setOnLongClickListener(longPress)
            actionInfo.setOnLongClickListener(longPress)
            actionRun.setOnLongClickListener(longPress)

            if (BuildConfig.BUILD_FOR_MARKET == AppBuildType.APK) {
                actionShare.apply {
                    setOnLongClickListener(longPress)
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
                actionShare.visibility = View.GONE
            }
            actionCopy.setOnClickListener {
                copyToClipboard(
                    "Name: ${data.name.toString()}\nPackage: ${data.pkg.toString()}\nSignature: ${data.signature.toString()}\n" +
                            "Version name: ${data.versionName.toString()}\n Version Code: ${data.versionCode}"
                )
                Toast.makeText(applicationContext, R.string.copied_to_clipboard, Toast.LENGTH_SHORT)
                    .show()
            }
            actionInfo.setOnClickListener {
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
            actionRun.setOnClickListener { ar ->
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
            data.meta.let {
                val title = resources.getString(R.string.details_metadata, it.size)
                moreMeta.text = title
                moreMeta.visibility = if (it.size > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                moreMeta.setOnClickListener { _ ->
                    MaterialDialog(this).show {
                        title(0, title)
                        customListAdapter(PropertiesDialogAdapter(it))
                    }
                }
            }
            data.activities.let {
                val title = resources.getString(R.string.details_activities, it.size)
                moreActivities.text = title
                moreActivities.visibility = if (it.size > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                moreActivities.setOnClickListener { _ ->
                    MaterialDialog(this).show {
                        title(0, title)
                        customListAdapter(PropertiesDialogAdapter(it))
                    }
                }
            }
            data.services.let {
                val title = resources.getString(R.string.details_services, it.size)
                moreServices.text = title
                moreServices.visibility = if (it.size > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                moreServices.setOnClickListener { _ ->
                    MaterialDialog(this).show {
                        title(0, title)
                        customListAdapter(PropertiesDialogAdapter(it))
                    }
                }
            }
            data.providers.let {
                val title = resources.getString(R.string.details_providers, it.size)
                moreProviders.text = title
                moreProviders.visibility = if (it.size > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                moreProviders.setOnClickListener { _ ->
                    MaterialDialog(this).show {
                        title(0, title)
                        customListAdapter(PropertiesDialogAdapter(it))
                    }
                }
            }
            data.receivers.let {
                val title = resources.getString(R.string.details_receivers, it.size)
                moreReceivers.text = title
                moreReceivers.visibility = if (it.size > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                moreReceivers.setOnClickListener { _ ->
                    MaterialDialog(this).show {
                        title(0, title)
                        customListAdapter(PropertiesDialogAdapter(it))
                    }
                }
            }
            data.directories.let {
                val title = resources.getString(R.string.details_directories, it.size)
                moreDirectories.text = title
                moreDirectories.visibility = if (it.size > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                moreDirectories.setOnClickListener { _ ->
                    MaterialDialog(this).show {
                        title(0, title)
                        customListAdapter(PropertiesDialogAdapter(it))
                    }
                }
            }
            data.permissions.let {
                val title = resources.getString(R.string.details_permissions, it.size)
                morePermissions.text = title
                morePermissions.visibility = if (it.size > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                morePermissions.setOnClickListener { _ ->
                    MaterialDialog(this).show {
                        title(0, title)
                        customListAdapter(PropertiesDialogAdapter(it))
                    }
                }
            }
            data.sharedLibraries.let {
                val title = resources.getString(R.string.details_shared_libraries, it.size)
                moreSharedLibraries.text = title
                moreSharedLibraries.visibility = if (it.size > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                moreSharedLibraries.setOnClickListener { _ ->
                    MaterialDialog(this).show {
                        title(0, title)
                        customListAdapter(PropertiesDialogAdapter(it))
                    }
                }
            }
            data.nativeLibraries.let {
                val title = resources.getString(R.string.details_native_libraries, it.size)
                moreNativeLibraries.text = title
                moreNativeLibraries.visibility = if (it.size > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                moreNativeLibraries.setOnClickListener { _ ->
                    MaterialDialog(this).show {
                        title(0, title)
                        customListAdapter(PropertiesDialogAdapter(it))
                    }
                }
            }
            moreOtherProperties.setOnClickListener {
                val props = ArrayList<String>()
                props.add(resources.getString(R.string.details_property_large_heap) + "\n" + data.isLargeHeap.toString())
                props.add(resources.getString(R.string.details_property_hw_accelerated) + "\n" + data.isHwAccelerated.toString())
                props.add(resources.getString(R.string.details_property_rtl_supported) + "\n" + data.isSupportRtl.toString())
                MaterialDialog(this).show {
                    title(R.string.details_other_properties)
                    customListAdapter(PropertiesDialogAdapter(props))
                }
            }
            loader.alpha = 1f
            loader.animate()
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
        pkg?.let {
            model.fetchInfo(pkg)
        } ?: run {
            // no package name
            finish()
        }

        toolbar.setTitle(R.string.app_details)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.navigationContentDescription = resources.getString(R.string.back)

        toolbar.menu.add(R.string.find_in_market).setOnMenuItemClickListener {
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

        if (savedInstanceState == null) {
            pkg?.let {
                logDetailsOpenCounter(it)
            }
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
            val fa = FirebaseAnalytics.getInstance(applicationContext)
            val value = Prefs(applicationContext).appDetailsOpenCounter
            fa.setUserId(UserInfo.setup(applicationContext, BuildConfig.VERSION_NAME, null).id)
            fa.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, Bundle().apply {
                putInt(FirebaseAnalytics.Param.VALUE, value)
                putString(FirebaseAnalytics.Param.ITEM_NAME, pkg)
            })
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
