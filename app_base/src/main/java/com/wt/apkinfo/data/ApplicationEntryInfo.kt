package com.wt.apkinfo.data

import android.os.Parcelable
import com.wt.apkinfo.proto.FilterAppType
import com.wt.apkinfo.proto.FilterInstaller
import kotlinx.parcelize.Parcelize

@Parcelize
class ApplicationEntryInfo constructor(
    val id: Long?,
    val pkg: String?,
    val name: String?,
    val icon: String?,
    val date: Long,
    val appType: FilterAppType,
    val installer: FilterInstaller,
    val targetSdk: Int
    ) : Parcelable {
    override fun toString(): String {
        return "{id=$id, pkg=$pkg, name=$name, icon=$icon, date=$date, appType=$appType, installer=$installer, targetSdk=$targetSdk}"
    }
}
