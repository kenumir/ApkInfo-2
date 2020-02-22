package com.wt.apkinfo.data

class ApplicationEntryInfo constructor(val id: Long?, val pkg: String?, val name: String?, val icon: String?) {
    override fun toString(): String {
        return "{id=$id, pkg=$pkg, name=$name, icon=$icon}"
    }
}