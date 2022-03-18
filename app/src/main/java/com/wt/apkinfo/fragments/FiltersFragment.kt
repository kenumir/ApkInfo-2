package com.wt.apkinfo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wt.apkinfo.R

class FiltersFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val res = inflater.inflate(R.layout.fragment_filters, container, false)
        (arguments?.getInt("title") ?: 0).let { title ->
            if (title > 0) {
                res.findViewById<TextView>(R.id.filterTitle).setText(title)
            }
        }

        return res
    }

    companion object {
        const val TAG = "FiltersFragment"


        fun create(titleRes: Int) : FiltersFragment {
            val res = FiltersFragment()
            res.arguments = Bundle().also {
                it.putInt("title", titleRes)
            }
            return res;
        }
    }
}