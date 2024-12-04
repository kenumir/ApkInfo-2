package com.wt.apkinfo.data

import com.wt.apkinfo.proto.FilterAppType
import com.wt.apkinfo.proto.FilterInstaller

class ApplicationEntryInfo constructor(
    val id: Long?,
    val pkg: String?,
    val name: String?,
    val icon: String?,
    val date: Long,
    val appType: FilterAppType,
    val installer: FilterInstaller,
    val targetSdk: Int
    ) {
    override fun toString(): String {
        return "{id=$id, pkg=$pkg, name=$name, icon=$icon, date=$date, appType=$appType, installer=$installer, targetSdk=$targetSdk}"
    }
}