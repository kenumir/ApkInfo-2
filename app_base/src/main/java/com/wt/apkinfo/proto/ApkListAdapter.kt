package com.wt.apkinfo.proto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wt.apkinfo.base.R
import com.wt.apkinfo.data.ApkFileEntryInfo

class ApkListAdapter(click: OnItemClick) : RecyclerView.Adapter<ApkListAdapter.Holder>() {

    private var items: List<ApkFileEntryInfo> = ArrayList()
    private var mOnItemClick = click

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_apk_file, parent, false), mOnItemClick)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.update(items[position])
    }

    fun swapData(newData: List<ApkFileEntryInfo>) {
        items = newData
        notifyDataSetChanged()
    }

    inner class Holder(itemView: View, listener: OnItemClick?) : RecyclerView.ViewHolder(itemView) {

        private val text1 = itemView.findViewById<TextView>(R.id.text1)
        private val text2 = itemView.findViewById<TextView>(R.id.text2)
        private var mData: ApkFileEntryInfo? = null
        private var mListener: OnItemClick? = listener

        init {
            itemView.setOnClickListener {
                mData?.let { _ ->
                    mListener?.let { i ->
                        mData?.let {
                            i.onItemClick(it)
                        }
                    }
                }
            }
        }
        fun update(data: ApkFileEntryInfo) {
            mData = data
            text1.text = data.size?.let { StringUtil.formatFileSize(it) }
            text2.text = data.name
        }
    }

    interface OnItemClick {
        fun onItemClick(item: ApkFileEntryInfo)
    }
}