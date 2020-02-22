package com.wt.apkinfo.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wt.apkinfo.R
import com.wt.apkinfo.data.ApkFileEntryInfo
import com.wt.apkinfo.data.models.ApkFilesViewModel
import com.wt.apkinfo.proto.ApkListAdapter
import kotlinx.android.synthetic.main.activity_apk_list.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.io.File


class ApkListActivity : AppCompatActivity() {

    private var adapter: ApkListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //overridePendingTransition(0, 0)
        setContentView(R.layout.activity_apk_list)

        val pkg = intent.getStringExtra("pkg")

        adapter = ApkListAdapter(object : ApkListAdapter.OnItemClick {
            override fun onItemClick(item: ApkFileEntryInfo) {
                shareApkFile(item.fullPath)
            }
        })
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.itemAnimator = null
        recycler.adapter = adapter

        pkg?.let {
            val model = ViewModelProvider(this).get<ApkFilesViewModel>(ApkFilesViewModel::class.java)
            model.getData().observe(this, Observer<List<ApkFileEntryInfo>> { data ->
                adapter?.swapData(data)
            })
            model.list(it)
        } ?: run {
            // no package name
            finish()
        }

        toolbar.setTitle(R.string.apk_list)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.navigationContentDescription = resources.getString(R.string.back)
    }

    private fun shareApkFile(f: String?) {
        f?.let {
            try {
                val apkFile = File(it)
                val apkURI: Uri = FileProvider.getUriForFile(
                    applicationContext,
                    applicationContext.packageName + ".fileprovider",
                    apkFile
                )
                val share = Intent()
                share.action = Intent.ACTION_SEND
                share.type = "application/vnd.android.package-archive"
                share.putExtra(Intent.EXTRA_STREAM, apkURI)
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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

    companion object {
        @JvmStatic
        fun show(ctx: Context, pkgName: String?)  {
            val it = Intent(ctx, ApkListActivity::class.java)
            it.putExtra("pkg", pkgName)
            ctx.startActivity(it)
        }
    }
}
