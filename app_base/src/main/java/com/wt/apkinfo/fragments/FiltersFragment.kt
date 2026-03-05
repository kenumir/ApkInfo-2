package com.wt.apkinfo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wt.apkinfo.base.R
import com.wt.apkinfo.base.databinding.FragmentFiltersBinding
import com.wt.apkinfo.proto.FilterType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FiltersFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFiltersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFiltersBinding.inflate(inflater, container, false)
        
        arguments?.let { args ->
            val filterType = args.getInt("filter_type", FilterType.SORT.ordinal)
            binding.filterTitle.setText(args.getInt("title"))
            
            args.getIntArray("items")?.let { items ->
                val onItemClick = View.OnClickListener { view ->
                    setFragmentResult("filter", bundleOf("result" to view.id, "filter_type" to filterType))
                    // Używamy lifecycleScope zamiast view.post dla bezpieczeństwa
                    lifecycleScope.launch {
                        delay(100) // krótka pauza na efekt kliknięcia
                        if (isAdded) dismissAllowingStateLoss()
                    }
                }
                
                var itemId = 0
                items.forEach { item ->
                    val layoutRes = when {
                        itemId == 0 -> R.layout.layout_filter_first
                        itemId >= items.size - 1 -> R.layout.layout_filter_last
                        else -> R.layout.layout_filter_middle
                    }
                    
                    val radioButton = inflater.inflate(layoutRes, binding.radios, false) as RadioButton
                    radioButton.apply {
                        setText(item)
                        id = itemId
                        setOnClickListener(onItemClick)
                    }
                    binding.radios.addView(radioButton)
                    
                    if (itemId < items.size - 1) {
                        binding.radios.addView(inflater.inflate(R.layout.layout_filter_divider, binding.radios, false))
                    }
                    itemId++
                }
                binding.radios.check(args.getInt("selected", 0))
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // Poprawka dla pełnego wyświetlania w trybie poziomym
        (dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet))?.let {
            BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FiltersFragment"

        fun create(titleRes: Int, items: IntArray, selected: Int, fType: FilterType): FiltersFragment {
            return FiltersFragment().apply {
                arguments = Bundle().also {
                    it.putInt("title", titleRes)
                    it.putIntArray("items", items)
                    it.putInt("selected", selected)
                    it.putInt("filter_type", fType.ordinal)
                }
            }
        }
    }
}