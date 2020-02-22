package com.wt.apkinfo.data.images

import android.content.Context
import android.widget.ImageView
import com.wt.apkinfo.proto.SingletonHolder

class ImageLoader private constructor(ctx: Context) {

    companion object : SingletonHolder<ImageLoader, Context>(::ImageLoader)

    private val impl: ImageLoading = PicassoImpl(ctx)

    fun load(url: String?, img: ImageView) {
        impl.load(url, img)
    }

}