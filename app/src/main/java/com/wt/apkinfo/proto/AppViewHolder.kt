package com.wt.apkinfo.proto

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wt.apkinfo.R
import com.wt.apkinfo.data.ApplicationEntryInfo

class AppViewHolder(itemView: View, mOnAppListItemClick: OnAppListItemClick?) : RecyclerView.ViewHolder(itemView) {

    private val icon: ImageView = itemView.findViewById(R.id.icon)
    private val text1: TextView = itemView.findViewById(R.id.text1)
    private val text2: TextView = itemView.findViewById(R.id.text2)
    private var mApplicationEntryInfo: ApplicationEntryInfo? = null

    init {
        itemView.setOnClickListener{
            mOnAppListItemClick?.onItemClick(mApplicationEntryInfo)
        }
    }

    fun update(data: ApplicationEntryInfo?) {
        mApplicationEntryInfo = data
        data?.let {
            text1.text = data.name
            text2.text = data.pkg
            icon.setImageDrawable(data.icon)
        }
    }

}