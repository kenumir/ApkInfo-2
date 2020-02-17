package com.wt.apkinfo.proto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.wt.apkinfo.R
import com.wt.apkinfo.data.ApplicationEntryInfo

class AppListAdapter(val mOnAppListItemClick: OnAppListItemClick) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<ApplicationEntryInfo?> = ArrayList<ApplicationEntryInfo>()

    init {
        setHasStableIds(true)
        (items as ArrayList).add(null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 2) {
            AppViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_application, parent, false), mOnAppListItemClick)
        } else {
            LoaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_loader, parent, false))
        }
    }

    override fun getItemId(position: Int): Long {
        return if (position == 0 && items[position] == null) {
            0 //Long.MAX_VALUE
        } else {
            val hash = items[position]?.pkg + "-" + items[position]?.name
            hash.hashCode().toLong()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && items[position] == null) {
            1
        } else {
            2
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AppViewHolder) {
            val h: AppViewHolder = holder
            h.update(items.get(position))
        } else if (holder is LoaderViewHolder) {
            // nothing
        }
    }

    fun swapData(newData: List<ApplicationEntryInfo>) {
        val result = DiffUtil.calculateDiff(AppListAdapterDiff(items, newData))
        items = newData
        result.dispatchUpdatesTo(this)
        //items = newData
        //notifyDataSetChanged()
    }

}