package com.wt.apkinfo.data.images

import android.widget.ImageView

interface ImageLoading {

    fun load(url: String?, img: ImageView)

}