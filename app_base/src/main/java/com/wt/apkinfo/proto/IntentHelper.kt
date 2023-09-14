package com.wt.apkinfo.proto

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Created by kenumir on 24.09.2017.
 *
 */
object IntentHelper {
    fun openInBrowser(ctx: Context, url: String): Boolean {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            ctx.startActivity(browserIntent)
            return true
        } catch (ignore: Exception) {
        }
        return false
    }
}