package com.wt.apkinfo.proto

import com.wt.apkinfo.data.ApplicationEntryInfo

interface OnAppListItemClick {
    fun onItemClick(item: ApplicationEntryInfo?)
}