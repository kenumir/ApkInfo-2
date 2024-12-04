package com.wt.apkinfo.proto

interface OnAdapterFilterData {
    fun getSort(): ListSortOrder
    fun getAppType(): FilterAppType
    fun getAppInstaller(): FilterInstaller
    fun getTargetSdk(): FilterTargetSdk
}