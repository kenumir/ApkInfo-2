package com.wt.apkinfo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wt.apkinfo.R
import com.wt.apkinfo.proto.FilterType

class FiltersFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val res = inflater.inflate(R.layout.fragment_filters, container, false)
        arguments?.let { args ->
            val filterType = args.getInt("filter_type", FilterType.SORT.ordinal)
            res.findViewById<TextView>(R.id.filterTitle).setText(args.getInt("title"))
            args.getIntArray("items")?.let { items ->
                val radios = res.findViewById<RadioGroup>(R.id.radios)
                val onItemClick = OnClickListener { view ->
                    setFragmentResult("filter", bundleOf("result" to view.id, "filter_type" to filterType))
                    view.post { dismiss() }
                }
                var itemId = 0
                items.forEach { item ->
                    when {
                        itemId == 0 -> {
                            radios.addView(inflater.inflate(R.layout.layout_filter_first, radios, false).apply {
                                (this as RadioButton).setText(item).apply {
                                    id = itemId
                                    setOnClickListener(onItemClick)
                                }
                            })
                            radios.addView(inflater.inflate(R.layout.layout_filter_divider, radios, false))
                        }
                        itemId >= items.size - 1 -> {
                            radios.addView(inflater.inflate(R.layout.layout_filter_last, radios, false).apply {
                                (this as RadioButton).setText(item).apply {
                                    id = itemId
                                    setOnClickListener(onItemClick)
                                }
                            })
                        }
                        else -> {
                            radios.addView(inflater.inflate(R.layout.layout_filter_middle, radios, false).apply {
                                (this as RadioButton).setText(item).apply {
                                    id = itemId
                                    setOnClickListener(onItemClick)
                                }
                            })
                            radios.addView(inflater.inflate(R.layout.layout_filter_divider, radios, false))
                        }
                    }
                    itemId++
                }
                radios.check(args.getInt("selected", 0))
            }
        }
        return res
    }

    companion object {

        const val TAG = "FiltersFragment"

        fun create(titleRes: Int, items:IntArray, selected: Int, fType: FilterType) : FiltersFragment {
            val res = FiltersFragment()
            res.arguments = Bundle().also {
                it.putInt("title", titleRes)
                it.putIntArray("items", items)
                it.putInt("selected", selected)
                it.putInt("filter_type", fType.ordinal)
            }
            return res;
        }
    }
}