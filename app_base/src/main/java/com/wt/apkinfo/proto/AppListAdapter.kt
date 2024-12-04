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
        return when (viewType) {
            2 -> {
                AppViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_application, parent, false),
                    mOnAppListItemClick
                )
            }
            3 -> {
                FilterViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_filter, parent, false),
                    mOnFilterItemClick
                )
            }
            4 -> {
                NoResultsHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_no_results, parent, false)
                )
            }
            else -> {
                LoaderViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_loader, parent, false)
                )
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return if (position == 0 && items[0] == null) {
            0
        } else if (items.isNotEmpty() && position == 0) {
            -1
        } else {
            if (position == 1 && items[0] != null && items[0]?.pkg == null) {
                Long.MAX_VALUE
            } else {
                val item = items[position - 1]
                (item?.pkg + "-" + item?.name + "-" + item?.icon + "-" + item?.date).hashCode().toLong()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && items[0] == null) {
            1
        } else {
            if (items.isNotEmpty() && position == 0) {
                3
            } else {
                if (position == 1 && items[0] != null && items[0]?.pkg == null) {
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AppViewHolder -> {
                holder.update(items[position-1])
            }
            is LoaderViewHolder -> {
                // nothing
            }
            is FilterViewHolder -> {
                holder.update(
                    mOnAdapterFilterData.getSort(),
                    mOnAdapterFilterData.getAppType(),
                    mOnAdapterFilterData.getAppInstaller(),
                    mOnAdapterFilterData.getTargetSdk()
                )
            }
        }
    }

    fun swapData(newData: List<ApplicationEntryInfo?>) {
        val result = DiffUtil.calculateDiff(AppListAdapterDiff(items, newData))
        items = newData
        result.dispatchUpdatesTo(this)
    }

}