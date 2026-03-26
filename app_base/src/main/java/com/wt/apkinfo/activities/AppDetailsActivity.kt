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
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.wt.apkinfo.app.AppBuildType
import com.wt.apkinfo.base.BuildConfig
import com.wt.apkinfo.base.R
import com.wt.apkinfo.base.databinding.ActivityAppDetailsBinding
import com.wt.apkinfo.data.ApplicationDetailsInfo
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.images.ImageLoader
import com.wt.apkinfo.data.models.ApplicationDetailsViewModel
import com.wt.apkinfo.data.models.DetailsUiState
import com.wt.apkinfo.era.ERA
import com.wt.apkinfo.proto.DateTime
import com.wt.apkinfo.proto.PropertiesDialogAdapter
import com.wt.apkinfo.proto.Utils

class AppDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppDetailsBinding
    private lateinit var viewModel: ApplicationDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            v.setPadding(
                insets.getInsets(WindowInsetsCompat.Type.statusBars()).left,
                insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).right,
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            )
            insets
        }

        binding.actionsCard.apply {
            setCardBackgroundColor(SurfaceColors.SURFACE_1.getColor(context))
        }

        val pkg = intent.getStringExtra("pkg") ?: return finish()
        viewModel = ViewModelProvider(this).get(ApplicationDetailsViewModel::class.java)
        
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is DetailsUiState.Loading -> binding.loader.visibility = View.VISIBLE
                is DetailsUiState.Success -> {
                    hideLoader()
                    updateUi(state.data)
                }
                is DetailsUiState.Error -> {
                    hideLoader()
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }

        viewModel.fetchInfo(pkg)
        setupActions(pkg)
    }

    private fun updateUi(data: ApplicationDetailsInfo) {
        binding.appName.text = data.name
        binding.appPackage.text = data.pkg
        binding.appVersionName.text = data.versionName
        binding.appVersionCode.text = data.versionCode.toString()
        binding.appSdkInfo.text = getString(R.string.details_sdk, data.sdkMin, data.sdkTarget)
        binding.appTime.text = getString(
            R.string.details_time, DateTime.formatFull(data.timeInstall), DateTime.formatFull(data.timeUpdate)
        )
        binding.appInstallerPackage.text = data.installerPackage
        
        updateAppTypeLabel(data)
        updateActionRunVisibility(data)
        ImageLoader.get(binding.appLogo.context).load(data.icon, binding.appLogo)
        setupShareAction(data)
        setupToolbar(data)
        setupDetailsSections(data)
        hideTvInterface()
    }

    private fun updateAppTypeLabel(data: ApplicationDetailsInfo) {
        when {
            data.isDebuggable -> {
                binding.appTypeInfo.apply {
                    visibility = View.VISIBLE
                    setText(R.string.app_type_debug)
                }
            }
            data.isSystemApp -> {
                binding.appTypeInfo.apply {
                    visibility = View.VISIBLE
                    setText(R.string.app_type_system)
                }
            }
            else -> binding.appTypeInfo.visibility = View.GONE
        }
    }

    private fun updateActionRunVisibility(data: ApplicationDetailsInfo) {
        binding.actionRun.visibility = if (Utils.isTV(this)) {
            if (data.launcherIntentTv == null) View.GONE else View.VISIBLE
        } else {
            if (data.launcherIntent == null) View.GONE else View.VISIBLE
        }
    }

    private fun setupShareAction(data: ApplicationDetailsInfo) {
        if (AppBuildType.APK == BuildConfig.BUILD_FOR_MARKET) {
            binding.actionShare.apply {
                visibility = View.VISIBLE
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
            binding.actionShare.visibility = View.GONE
        }
    }

    private fun setupToolbar(data: ApplicationDetailsInfo) {
        binding.toolbar.toolbar.apply {
            title = getString(R.string.app_details)
            setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            setNavigationOnClickListener { finish() }
            
            menu.clear()
            setupToolbarMenu(this, data)
        }
    }

    private fun setupToolbarMenu(toolbar: androidx.appcompat.widget.Toolbar, data: ApplicationDetailsInfo) {
        toolbar.menu.apply {
            add(R.string.find_in_market).setOnMenuItemClickListener {
                handleMarketClick(data.pkg ?: "")
                true
            }
            add(0, 1, 0, R.string.copy).setOnMenuItemClickListener {
                copyToClipboard(
                    "Name: ${data.name}\nPackage: ${data.pkg}\nSignature: ${data.signature}\n" +
                    "Version name: ${data.versionName}\nVersion Code: ${data.versionCode}"
                )
                Toast.makeText(applicationContext, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
                true
            }
            if (AppBuildType.APK == BuildConfig.BUILD_FOR_MARKET) {
                add(0, 2, 0, R.string.share).setOnMenuItemClickListener {
                    startActivity(Intent().apply {
                        component = ComponentName(packageName, "$packageName.activities.ApkListActivity")
                        putExtra("pkg", data.pkg)
                        putExtra("version_name", data.versionName)
                        putExtra("version_code", data.versionCode)
                    })
                    true
                }
            }
        }
        if (Utils.isTV(toolbar.context) && AppBuildType.APK != BuildConfig.BUILD_FOR_MARKET) {
            toolbar.visibility = View.GONE
        }
    }

    private fun setupActions(pkg: String) {
        binding.actionUninstall.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkg")))
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.app_run_error, e.message), Toast.LENGTH_LONG).show()
            }
        }

        binding.actionInfo.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg")))
            } catch (e: Exception) {
                ERA.logException(e)
                try {
                    startActivity(Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
                } catch (w2: Exception) {
                    Toast.makeText(this, getString(R.string.app_run_error, e.message), Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.actionRun.setOnClickListener {
            val state = viewModel.uiState.value
            if (state is DetailsUiState.Success) {
                val intent = if (Utils.isTV(this)) state.data.launcherIntentTv else state.data.launcherIntent
                intent?.let {
                    try {
                        startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (e: Exception) {
                        ERA.logException(e)
                        Toast.makeText(this, getString(R.string.app_run_error, e.message), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupDetailsSections(data: ApplicationDetailsInfo) {
        setupSection(binding.moreMeta, R.string.details_metadata, data.meta)
        setupSection(binding.moreActivities, R.string.details_activities, data.activities)
        setupSection(binding.moreServices, R.string.details_services, data.services)
        setupSection(binding.moreProviders, R.string.details_providers, data.providers)
        setupSection(binding.moreReceivers, R.string.details_receivers, data.receivers)
        setupSection(binding.morePermissions, R.string.details_permissions, data.permissions)
        
        binding.moreOtherProperties.setOnClickListener {
            val props = arrayListOf(
                getString(R.string.details_property_large_heap) + "\n" + data.isLargeHeap,
                getString(R.string.details_property_hw_accelerated) + "\n" + data.isHwAccelerated,
                getString(R.string.details_property_rtl_supported) + "\n" + data.isSupportRtl
            )
            showListDialog(getString(R.string.details_other_properties), props)
        }
    }

    private fun setupSection(view: androidx.appcompat.widget.AppCompatTextView, titleRes: Int, items: List<String>) {
        val title = getString(titleRes, items.size)
        view.text = title
        val isTv = Utils.isTV(this)
        view.visibility = if (items.isNotEmpty() && (!isTv || titleRes != R.string.details_metadata && titleRes != R.string.details_activities)) View.VISIBLE else View.GONE
        view.setOnClickListener { showListDialog(title, items) }
    }

    private fun showListDialog(title: String, items: List<String>) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setView(RecyclerView(this).apply {
                layoutManager = LinearLayoutManager(this@AppDetailsActivity)
                adapter = PropertiesDialogAdapter(ArrayList(items))
            })
            .show()
    }

    private fun handleMarketClick(pkg: String) {
        val uri = if (AppBuildType.HUAWEI == BuildConfig.BUILD_FOR_MARKET) {
            Uri.parse("appmarket://details?id=$pkg")
        } else {
            Uri.parse("market://details?id=$pkg")
        }
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
                if (AppBuildType.GOOGLE == BuildConfig.BUILD_FOR_MARKET) setPackage("com.android.vending")
            })
        } catch (e: Exception) {
            val webUri = if (AppBuildType.HUAWEI == BuildConfig.BUILD_FOR_MARKET) {
                Uri.parse("https://appgallery.huawei.com/app/C101754683")
            } else {
                Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
            }
            try {
                startActivity(Intent(Intent.ACTION_VIEW, webUri))
            } catch (e2: Exception) {
                ERA.logException(Exception("No Market app and WebBrowser"))
            }
        }
    }

    private fun hideLoader() {
        binding.loader.animate()
            .alpha(0f)
            .setDuration(250)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) { binding.loader.visibility = View.GONE }
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationStart(animation: Animator) {}
            })
    }

    private fun hideTvInterface() {
        if (Utils.isTV(this) && AppBuildType.APK != BuildConfig.BUILD_FOR_MARKET) {
            val views = listOf(
                binding.moreInfoHeader, binding.moreMeta, binding.morePermissions,
                binding.moreActivities, binding.moreServices, binding.moreProviders,
                binding.moreReceivers, binding.moreDirectories, binding.moreSharedLibraries,
                binding.moreNativeLibraries, binding.moreOtherProperties
            )
            views.forEach { it.visibility = View.GONE }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("ApkInfo", text))
    }

    companion object {
        @JvmStatic
        fun show(ctx: Context, appInfo: ApplicationEntryInfo?) {
            val intent = Intent(ctx, AppDetailsActivity::class.java).apply {
                putExtra("pkg", appInfo?.pkg)
            }
            ctx.startActivity(intent)
        }
    }
}
