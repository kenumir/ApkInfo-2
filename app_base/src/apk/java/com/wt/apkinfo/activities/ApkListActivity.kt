package com.wt.apkinfo.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wt.apkinfo.base.R
import com.wt.apkinfo.base.databinding.ActivityApkListBinding
import com.wt.apkinfo.data.ApkFileEntryInfo
import com.wt.apkinfo.data.models.ApkFilesUiState
import com.wt.apkinfo.data.models.ApkFilesViewModel
import com.wt.apkinfo.data.models.ArchiveUiState
import com.wt.apkinfo.data.models.ArchiveViewModel
import com.wt.apkinfo.proto.ApkListAdapter
import java.io.File


class ApkListActivity : AppCompatActivity() {

    companion object {
        const val KEY_PKG = "pkg"
        const val KEY_VERSION_NAME = "version_name"
        const val KEY_VERSION_CODE = "version_code"
    }

    private var mApkListAdapter: ApkListAdapter? = null
    private var archiveMenuItem: MenuItem? = null
    private lateinit var archiveViewModel: ArchiveViewModel
    private lateinit var apkFilesViewModel: ApkFilesViewModel
    private lateinit var binding: ActivityApkListBinding

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityApkListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainView) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(bars.left, bars.top, bars.right, 0)
            insets
        }

        val pkg = intent.getStringExtra(KEY_PKG) ?: return finish()
        val versionName = intent.getStringExtra(KEY_VERSION_NAME)
        val versionCode = intent.getIntExtra(KEY_VERSION_CODE, 0)

        setupRecyclerView()
        setupViewModels(pkg, versionName, versionCode)
        setupToolbar(pkg, versionName, versionCode)
    }

    private fun setupRecyclerView() {
        mApkListAdapter = ApkListAdapter(object : ApkListAdapter.OnItemClick {
            override fun onItemClick(item: ApkFileEntryInfo) {
                shareApkFile(item.fullPath)
            }
        })
        
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(this@ApkListActivity)
            itemAnimator = null
            adapter = mApkListAdapter
        }
    }

    private fun setupViewModels(pkg: String, versionName: String?, versionCode: Int) {
        apkFilesViewModel = ViewModelProvider(this).get(ApkFilesViewModel::class.java)
        archiveViewModel = ViewModelProvider(this).get(ArchiveViewModel::class.java)

        apkFilesViewModel.uiState.observe(this) { state ->
            when (state) {
                is ApkFilesUiState.Loading -> { /* show loader if needed */ }
                is ApkFilesUiState.Success -> {
                    mApkListAdapter?.swapData(state.items)
                    archiveMenuItem?.isVisible = true
                }
                is ApkFilesUiState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        archiveViewModel.uiState.observe(this) { state ->
            when (state) {
                is ArchiveUiState.Idle -> {
                    archiveMenuItem?.actionView = null
                }
                is ArchiveUiState.Loading -> {
                    archiveMenuItem?.actionView = LayoutInflater.from(this)
                        .inflate(R.layout.loader_toolbar_icon, null, false)
                }
                is ArchiveUiState.Success -> {
                    archiveMenuItem?.actionView = null
                    shareZipFile(state.file)
                    archiveViewModel.resetState()
                }
                is ArchiveUiState.Error -> {
                    archiveMenuItem?.actionView = null
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    archiveViewModel.resetState()
                }
            }
        }

        apkFilesViewModel.list(pkg)
    }

    private fun setupToolbar(pkg: String, versionName: String?, versionCode: Int) {
        binding.toolbar.toolbar.apply {
            setTitle(R.string.apk_list)
            setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            navigationContentDescription = resources.getString(R.string.back)
            
            archiveMenuItem = menu.add(R.string.share_as_zip)
                .setVisible(false)
                .setIcon(R.drawable.archive_arrow_down)
                .setOnMenuItemClickListener {
                    archiveViewModel.startMakeArchive(pkg, versionName, versionCode)
                    true
                }
            archiveMenuItem?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
    }

    private fun shareZipFile(zipFile: File) {
        FileProvider.getUriForFile(
            applicationContext,
            "${applicationContext.packageName}.fileprovider",
            zipFile
        )?.let { uri ->
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = contentResolver.getType(uri)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
        }
    }

    private fun shareApkFile(f: String?) {
        f?.let {
            try {
                val apkFile = File(it)
                val apkURI = FileProvider.getUriForFile(
                    applicationContext,
                    "${applicationContext.packageName}.fileprovider",
                    apkFile
                )
                val share = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, apkURI)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(share, getString(R.string.share)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
