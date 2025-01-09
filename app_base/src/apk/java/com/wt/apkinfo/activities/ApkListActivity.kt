package com.wt.apkinfo.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wt.apkinfo.base.R
import com.wt.apkinfo.data.ApkFileEntryInfo
import com.wt.apkinfo.data.models.ApkFilesViewModel
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
    private var mArchiveViewModel: ArchiveViewModel? = null
    private var mApkFilesViewModel: ApkFilesViewModel? = null

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apk_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainView)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(bars.left, bars.top, bars.right, 0)
            insets
        }

        val pkg = intent.getStringExtra(KEY_PKG)
        val versionName = intent.getStringExtra(KEY_VERSION_NAME)
        val versionCode = intent.getIntExtra(KEY_VERSION_CODE, 0)

        mApkListAdapter = ApkListAdapter(object : ApkListAdapter.OnItemClick {
            override fun onItemClick(item: ApkFileEntryInfo) {
                shareApkFile(item.fullPath)
            }
        })
        findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(this@ApkListActivity)
            itemAnimator = null
            adapter = mApkListAdapter
        }

        pkg?.let {
            mApkFilesViewModel = ViewModelProvider(this).get(ApkFilesViewModel::class.java).also { m ->
                m.getData().observe(this) { data ->
                    mApkListAdapter?.swapData(data)
                    archiveMenuItem?.isVisible = true
                }
                m.list(it)
            }

            ViewModelProvider(this).get(ArchiveViewModel::class.java).let { m ->
                mArchiveViewModel = m.also { mm ->
                    mm.getProgress().observe(this) { data ->
                        archiveMenuItem?.actionView = if (data) {
                            LayoutInflater.from(this@ApkListActivity)
                                .inflate(R.layout.loader_toolbar_icon, null, false)
                        } else {
                            mm.getLastDataResult()?.let { shareFileName ->
                                val archivePath = File(cacheDir, "archives")
                                val zipFile = File(archivePath, shareFileName)
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
                                    startActivity(
                                        Intent.createChooser(
                                            shareIntent,
                                            resources.getString(R.string.share)
                                        )
                                    )
                                }
                            }
                            null
                        }
                    }
                }
            }

        } ?: run {
            // no package name
            finish()
        }

        findViewById<Toolbar>(R.id.toolbar).apply {
            setTitle(R.string.apk_list)
            setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            setNavigationOnClickListener {
                finish()
            }
            navigationContentDescription = resources.getString(R.string.back)
            archiveMenuItem = menu
                .add(R.string.share_as_zip)
                .setVisible(false)
                .setIcon(R.drawable.archive_arrow_down)
                .setOnMenuItemClickListener {
                    pkg?.let { p ->
                        mArchiveViewModel?.startMakeArchive(p, versionName, versionCode)
                    } ?: run {
                        archiveMenuItem?.actionView = null
                    }
                    true
                }.apply {
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                }
        }
    }

    private fun shareApkFile(f: String?) {
        f?.let {
            try {
                val apkFile = File(it)
                val apkURI: Uri = FileProvider.getUriForFile(
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
                startActivity(
                    Intent.createChooser(
                        share,
                        resources.getString(R.string.share)
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
