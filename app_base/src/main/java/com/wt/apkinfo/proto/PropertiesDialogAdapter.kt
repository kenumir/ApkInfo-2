package com.wt.apkinfo.proto

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.wt.apkinfo.base.BuildConfig
import com.wt.apkinfo.base.R
import com.wt.apkinfo.era.ERA

class PropertiesDialogAdapter(private val items: ArrayList<String>) : RecyclerView.Adapter<PropertiesDialogAdapter.Holder>() {

    //private val exec = Executors.newCachedThreadPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_property, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.update(items[position])
    }

    private fun copyToClipboard(text: String, ctx: Context) {
        //exec.execute {
            val clipboard: ClipboardManager = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ApkInfo", text)
            clipboard.setPrimaryClip(clip)
        //}
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1 = itemView.findViewById<TextView>(R.id.text1)
        private val text2 = itemView.findViewById<TextView>(R.id.text2)
        private val text3 = itemView.findViewById<TextView>(R.id.text3)
        init {
            itemView.setOnClickListener{
                var text = ""
                if (text1.visibility == View.VISIBLE) {
                    text = text1.text.toString()
                }
                if (text2.visibility == View.VISIBLE) {
                    text = text + (if (text.isNotEmpty()) " - " else "") +  text2.text.toString()
                }
                if (text3.visibility == View.VISIBLE) {
                    text = text + (if (text.isNotEmpty()) " - " else "") +  text3.text.toString()
                }

                try {
                    copyToClipboard(text, it.context)
                    Toast.makeText(it.context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT)
                        .show()
                } catch (e: Exception) {
                    ERA.logException(e)
                    Toast.makeText(it.context, "Copy error", Toast.LENGTH_SHORT)
                        .show()
                }

                if (BuildConfig.DEBUG) {
                    Log.i("ApkInfo", "Copy: $text")
                }
            }
        }
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