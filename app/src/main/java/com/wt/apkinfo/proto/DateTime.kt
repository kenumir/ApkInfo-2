package com.wt.apkinfo.proto

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by kenumir on 24.09.2017.
 *
 */
object DateTime {
    const val FORMAT_FULL = "yyyy-MM-dd HH:mm:ss"
    fun formatFull(timestamp: Long?): String {
        return format(
            timestamp,
            FORMAT_FULL
        )
    }

    fun format(timestamp: Long?, format: String): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(timestamp)
    }
}