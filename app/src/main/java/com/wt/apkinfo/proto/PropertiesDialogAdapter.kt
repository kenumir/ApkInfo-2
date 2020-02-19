package com.wt.apkinfo.proto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wt.apkinfo.R

class PropertiesDialogAdapter(list: ArrayList<String>) : RecyclerView.Adapter<PropertiesDialogAdapter.Holder>() {

    private val items = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_property, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.update(items[position])
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1 = itemView.findViewById<TextView>(R.id.text1)
        private val text2 = itemView.findViewById<TextView>(R.id.text2)
        private val text3 = itemView.findViewById<TextView>(R.id.text3)
        fun update(text: String) {
            val lines = text.split("\n")
            when(lines.size) {
                1 -> {
                    text2.text = lines[0]
                    text1.visibility = View.GONE
                    text3.visibility = View.GONE
                }
                2 -> {
                    text1.text = lines[0]
                    text2.text = lines[1]
                    text1.visibility = View.VISIBLE
                    text3.visibility = View.GONE
                }
                3 -> {
                    text1.text = lines[0]
                    text2.text = lines[1]
                    text3.text = lines[2]
                    text1.visibility = View.VISIBLE
                    text3.visibility = View.VISIBLE
                }
            }
        }
    }
}