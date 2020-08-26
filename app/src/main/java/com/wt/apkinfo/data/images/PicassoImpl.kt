package com.wt.apkinfo.data.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import com.wt.apkinfo.BuildConfig
import java.io.IOException

class PicassoImpl(ctx: Context) : ImageLoading {

    private val mPicasso: Picasso

    init {
        val pm = ctx.applicationContext.packageManager
        mPicasso = Picasso.Builder(ctx)
            .memoryCache(LruCache(ctx))
            .addRequestHandler(object : RequestHandler() {
                override fun canHandleRequest(data: Request?): Boolean {
                    data?.let { it1 ->
                        it1.uri?.let { it2 ->
                            return "app" == it2.scheme
                        }
                    }
                    return false
                }

                override fun load(request: Request?, networkPolicy: Int): Result? {
                    try {
                        request?.let { it1 ->
                            it1.uri?.let { it2 ->
                                it2.host?.let { it3 ->
                                    val d: Drawable = pm.getApplicationIcon(it3)
                                    val b = Bitmap.createBitmap(
                                        d.intrinsicWidth,
                                        d.intrinsicHeight,
                                        Bitmap.Config.ARGB_8888
                                    )
                                    val canvas = Canvas(b)
                                    d.setBounds(0, 0, canvas.width, canvas.height)
                                    d.draw(canvas)
                                    return Result(b, Picasso.LoadedFrom.DISK)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        if (BuildConfig.DEBUG) {
                            Log.e("apkinfo", "PicassoImpl: load error=$e", e)
                        }
                        throw IOException(e)
                    }
                    return null
                }
            })
            .build()
    }

    override fun load(url: String?, img: ImageView) {
        mPicasso.cancelRequest(img)
        url?.let {
            mPicasso.load(it)
                .fit()
                .centerInside()
                .into(img)
        }
    }
}