package com.wt.apkinfo.proto

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wt.apkinfo.base.R

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

    fun update(sort: ListSortOrder, appType: FilterAppType, installer: FilterInstaller) {
        filter_sort.text = "${itemView.resources.getString(R.string.sort)}: " +
            when (sort) {
                ListSortOrder.DATE -> itemView.resources.getString(R.string.sort_date)
                ListSortOrder.PACKAGE -> itemView.resources.getString(R.string.sort_package)
                ListSortOrder.NAME -> itemView.resources.getString(R.string.sort_name)
            }

        filter_type.text = "${itemView.resources.getString(R.string.type)}: " + when (appType) {
            FilterAppType.ALL -> itemView.resources.getString(R.string.type_all)
            FilterAppType.SYSTEM -> itemView.resources.getString(R.string.type_system)
            FilterAppType.DEBUG -> itemView.resources.getString(R.string.type_debug)
            FilterAppType.USER -> itemView.resources.getString(R.string.type_user)
        }

        filter_installer.text = "${itemView.resources.getString(R.string.installer)}: " + when (installer) {
            FilterInstaller.ALL -> itemView.resources.getString(R.string.installer_all)
            FilterInstaller.PLAY_STORE -> itemView.resources.getString(R.string.installer_vending)
            FilterInstaller.APP_GALLERY -> itemView.resources.getString(R.string.installer_huawei)
            FilterInstaller.EMPTY -> itemView.resources.getString(R.string.installer_empty)
        }
    }

}