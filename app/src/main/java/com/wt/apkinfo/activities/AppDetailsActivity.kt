package com.wt.apkinfo.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.appbar.MaterialToolbar
import com.wt.apkinfo.R
import com.wt.apkinfo.data.ApplicationEntryInfo

class AppDetailsActivity : AppCompatActivity() {

    private var toolbar: MaterialToolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.activity_app_details)
        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitle(R.string.app_details)
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
