package com.wt.apkinfo.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wt.apkinfo.R
import com.wt.apkinfo.data.ApkFileEntryInfo
import com.wt.apkinfo.data.models.ApkFilesViewModel
import com.wt.apkinfo.data.models.ArchiveViewModel
import com.wt.apkinfo.proto.ApkListAdapter
import kotlinx.android.synthetic.main.activity_apk_list.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.io.File


class ApkListActivity : AppCompatActivity() {

    companion object {

        const val KEY_PKG = "pkg"
        const val KEY_VERSION_NAME = "version_name"
        const val KEY_VERSION_CODE = "version_code"

        @JvmStatic
        fun show(ctx: Context, pkgName: String?, versionName: String?, version: Int)  {
            val it = Intent(ctx, ApkListActivity::class.java)
            it.putExtra(KEY_PKG, pkgName)
            it.putExtra(KEY_VERSION_NAME, versionName)
            it.putExtra(KEY_VERSION_CODE, version)
            ctx.startActivity(it)
        }
    }

    private var mApkListAdapter: ApkListAdapter? = null
    private var archiveMenuItem: MenuItem? = null
    private var mArchiveViewModel: ArchiveViewModel? = null
    private var mApkFilesViewModel: ApkFilesViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apk_list)

        val pkg = intent.getStringExtra(KEY_PKG)
        val versionName = intent.getStringExtra(KEY_VERSION_NAME)
        val versionCode = intent.getIntExtra(KEY_VERSION_CODE, 0)

        mApkListAdapter = ApkListAdapter(object : ApkListAdapter.OnItemClick {
            override fun onItemClick(item: ApkFileEntryInfo) {
                shareApkFile(item.fullPath)
            }
        })
        recycler.apply {
            layoutManager = LinearLayoutManager(this@ApkListActivity)
            itemAnimator = null
            adapter = mApkListAdapter
        }

        pkg?.let {
            mApkFilesViewModel = ViewModelProvider(this).get(ApkFilesViewModel::class.java).also { m ->
                m.getData().observe(this, { data ->
                    mApkListAdapter?.swapData(data)
                    archiveMenuItem?.isVisible = true
                })
                m.list(it)
            }

            ViewModelProvider(this).get(ArchiveViewModel::class.java).let { m ->
                mArchiveViewModel = m.also { mm ->
                    mm.getProgress().observe(this, { data ->
                        archiveMenuItem?.actionView = if (data) {
                            LayoutInflater.from(this@ApkListActivity).inflate(R.layout.loader_toolbar_icon, null, false)
                        } else {
                            mm.getLastDataResult()?.let { shareFileName ->
                                val archivePath: File = File(cacheDir, "archives")
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
                                    startActivity(Intent.createChooser(
                                        shareIntent,
                                        resources.getString(R.string.share)
                                    ))
                                }
                            }
                            null
                        }
                    })
                }
            }

        } ?: run {
            // no package name
            finish()
        }


        toolbar.apply {
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
