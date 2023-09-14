package com.wt.apkinfo.proto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.wt.apkinfo.base.R
import com.wt.apkinfo.data.ApplicationEntryInfo

class AppListAdapter(
    private val mOnAppListItemClick: OnAppListItemClick,
    private val mOnFilterItemClick: OnFilterItemClick,
    private val mOnAdapterFilterData: OnAdapterFilterData
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<ApplicationEntryInfo?> = ArrayList()

    init {
        setHasStableIds(true)
        (items as ArrayList).add(null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 2) {
            AppViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_application, parent, false),
                mOnAppListItemClick
            )
        } else if (viewType == 3) {
            FilterViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_filter, parent, false),
                mOnFilterItemClick
            )
        } else if (viewType == 4) {
            NoResultsHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_no_results, parent, false)
            )
        } else {
            LoaderViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loader, parent, false)
            )
        }
    }

    override fun getItemId(position: Int): Long {
        return if (position == 0 && items[position] == null) {
            0
        } else if (items.isNotEmpty() && position == 0) {
            -1
        } else {
            if (position == 1 && items[position-1] != null && items[position-1]?.pkg == null) {
                Long.MAX_VALUE
            } else {
                val pos = position - 1
                (items[pos]?.pkg + "-" + items[pos]?.name).hashCode().toLong()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && items[position] == null) {
            1
        } else {
            if (items.isNotEmpty() && position == 0) {
                3
            } else {
                if (position == 1 && items[position-1] != null && items[position-1]?.pkg == null) {
                    4
                } else {
                    2
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (items.isNotEmpty()) items.size + 1 else 0
    }

    @Suppress("ControlFlowWithEmptyBody")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AppViewHolder) {
            val h: AppViewHolder = holder
            h.update(items[position-1])
        } else if (holder is LoaderViewHolder) {
            // nothing
        } else if (holder is FilterViewHolder) {
            holder.update(
                mOnAdapterFilterData.getSort(),
                mOnAdapterFilterData.getAppType(),
                mOnAdapterFilterData.getAppInstaller()
            )
        }
    }

    fun swapData(newData: List<ApplicationEntryInfo?>) {
        val result = DiffUtil.calculateDiff(AppListAdapterDiff(items, newData))
        items = newData
        result.dispatchUpdatesTo(this)
    }

}