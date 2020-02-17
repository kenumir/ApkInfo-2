package com.wt.apkinfo.proto

import androidx.recyclerview.widget.DiffUtil
import com.wt.apkinfo.data.ApplicationEntryInfo

class AppListAdapterDiff(
    private val oldItems: List<ApplicationEntryInfo?>,
    private val newItems: List<ApplicationEntryInfo?>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val o = oldItems[oldItemPosition]
        val n = newItems[newItemPosition]
        if (o == null && n != null || o != null && n == null) {
            return false;
        }
        return true //o.javaClass.name == n.javaClass.name
    }

    override fun getOldListSize(): Int {
        return oldItems.size
    }

    override fun getNewListSize(): Int {
        return newItems.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val o = oldItems[oldItemPosition]
        val n = newItems[newItemPosition]
        return o?.pkg.equals(n?.pkg) && o?.name.equals(n?.name)
    }
}