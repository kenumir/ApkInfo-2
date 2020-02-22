package com.wt.apkinfo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wt.apkinfo.R
import com.wt.apkinfo.fragments.AppListFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        supportFragmentManager.findFragmentByTag("AppListFragment_Tag")?.let {
            if (it is AppListFragment) {
                if (it.onBackAction()) {
                    return
                }
            }
        }
        super.onBackPressed()
    }
}
