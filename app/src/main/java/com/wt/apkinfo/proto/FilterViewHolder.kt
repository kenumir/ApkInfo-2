package com.wt.apkinfo.proto

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wt.apkinfo.R

class FilterViewHolder(itemView: View, click: OnFilterItemClick?) : RecyclerView.ViewHolder(itemView) {

    val filter_sort = itemView.findViewById<TextView>(R.id.filter_sort)
    val filter_type = itemView.findViewById<TextView>(R.id.filter_type)
    val filter_installer = itemView.findViewById<TextView>(R.id.filter_installer)

    init {
        filter_sort.setOnClickListener {
            click?.onItemClick(FilterType.SORT)
        }
        filter_type.setOnClickListener {
            click?.onItemClick(FilterType.TYPE)
        }
        filter_installer.setOnClickListener {
            click?.onItemClick(FilterType.INSTALLER)
        }
    }

    fun update() {

    }

}