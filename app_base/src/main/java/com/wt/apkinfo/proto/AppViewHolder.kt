package com.wt.apkinfo.proto

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wt.apkinfo.base.R
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.images.ImageLoader
import java.text.SimpleDateFormat
import java.util.*

class AppViewHolder(itemView: View, mOnAppListItemClick: OnAppListItemClick?) : RecyclerView.ViewHolder(itemView) {

    private val icon: ImageView = itemView.findViewById(R.id.icon)
    private val text1: TextView = itemView.findViewById(R.id.text1)
    private val text2: TextView = itemView.findViewById(R.id.text2)
    private val text3: TextView = itemView.findViewById(R.id.text3)
    private var mApplicationEntryInfo: ApplicationEntryInfo? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    init {
        itemView.setOnClickListener{
            mOnAppListItemClick?.onItemClick(mApplicationEntryInfo)
        }
    }

    fun update(data: ApplicationEntryInfo?) {
        mApplicationEntryInfo = data
        icon.setImageBitmap(null)
        data?.let {
            text1.text = it.name
            text2.text = it.pkg
            text3.text = if (it.date == 0L) "-" else dateFormat.format(it.date)
            ImageLoader.get(icon.context).load(it.icon, icon)
        }
    }

}