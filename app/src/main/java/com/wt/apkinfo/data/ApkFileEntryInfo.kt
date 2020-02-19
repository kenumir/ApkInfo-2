package com.wt.apkinfo.data

import java.io.File

class ApkFileEntryInfo constructor(val name: String?, val fullPath: String?, val size: Long?) {
    companion object {
        @JvmStatic
        fun fromFile(f: File): ApkFileEntryInfo  {
            return ApkFileEntryInfo(f.name, f.absolutePath, f.length());
        }
    }
}