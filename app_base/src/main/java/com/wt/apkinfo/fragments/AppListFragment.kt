package com.wt.apkinfo.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wt.apkinfo.activities.AppDetailsActivity
import com.wt.apkinfo.base.BuildConfig
import com.wt.apkinfo.base.R
import com.wt.apkinfo.base.databinding.FragmentAppListBinding
import com.wt.apkinfo.base.databinding.LayoutSearchBinding
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.Prefs
import com.wt.apkinfo.data.models.ApplicationsViewModel
import com.wt.apkinfo.proto.AppListAdapter
import com.wt.apkinfo.proto.FilterAppType
import com.wt.apkinfo.proto.FilterInstaller
import com.wt.apkinfo.proto.FilterTargetSdk
import com.wt.apkinfo.proto.FilterType
import com.wt.apkinfo.proto.IntentHelper
import com.wt.apkinfo.proto.ListSortOrder
import com.wt.apkinfo.proto.OnAppListItemClick
import com.wt.apkinfo.proto.OnFilterItemClick
import com.wt.apkinfo.proto.Themes
import com.wt.apkinfo.proto.Utils

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val SAVE_SEARCH_QUERY = "search_query"

class AppListFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentAppListBinding? = null
    private val binding get() = _binding!!

    private var searchBinding: LayoutSearchBinding? = null

    private var adapter: AppListAdapter = AppListAdapter(object : OnAppListItemClick {
        override fun onItemClick(item: ApplicationEntryInfo?) {
            if (item?.pkg != null) {
                activity?.let { AppDetailsActivity.show(it, item) }
            }
        }
    })

    private lateinit var model: ApplicationsViewModel
    private var searchMenu: MenuItem? = null

    private val onFilterItemClick = object: OnFilterItemClick {
        override fun onItemClick(item: FilterType) {
            val titleRes = when (item) {
                FilterType.SORT -> R.string.sort
                FilterType.TYPE -> R.string.type
                FilterType.INSTALLER -> R.string.installer
                FilterType.TARGET_SDK -> R.string.filter_target_sdk
            }
            val itemsRes = when (item) {
                FilterType.SORT -> intArrayOf(R.string.sort_name, R.string.sort_date, R.string.sort_package)
                FilterType.TYPE -> intArrayOf(R.string.type_all, R.string.type_user, R.string.type_debug, R.string.type_system)
                FilterType.INSTALLER -> intArrayOf(R.string.installer_all, R.string.installer_vending, R.string.installer_huawei, R.string.installer_empty)
                FilterType.TARGET_SDK -> intArrayOf(R.string.filter_target_sdk_all, R.string.filter_target_sdk_36, R.string.filter_target_sdk_35, R.string.filter_target_sdk_34, R.string.filter_target_sdk_33)
            }
            val selPos = activity?.let {
                when (item) {
                    FilterType.SORT -> when (Prefs(it).listSortOrder) {
                        ListSortOrder.DATE -> 1
                        ListSortOrder.PACKAGE -> 2
                        else -> 0
                    }
                    FilterType.TYPE -> when (Prefs(it).listFilterAppType) {
                        FilterAppType.ALL -> 0
                        FilterAppType.USER -> 1
                        FilterAppType.DEBUG -> 2
                        FilterAppType.SYSTEM -> 3
                    }
                    FilterType.INSTALLER -> when (Prefs(it).listFilterInstaller) {
                        FilterInstaller.ALL -> 0
                        FilterInstaller.PLAY_STORE -> 1
                        FilterInstaller.APP_GALLERY -> 2
                        FilterInstaller.EMPTY -> 3
                    }
                    FilterType.TARGET_SDK -> when (Prefs(it).listFilterTargetSdk) {
                        FilterTargetSdk.ALL -> 0
                        FilterTargetSdk.API_36 -> 1
                        FilterTargetSdk.API_35 -> 2
                        FilterTargetSdk.API_34 -> 3
                        FilterTargetSdk.API_33 -> 4
                    }
                }
            } ?: 0
            val modalBottomSheet = FiltersFragment.create(titleRes, itemsRes, selPos, item)
            modalBottomSheet.show(parentFragmentManager, FiltersFragment.TAG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        model = ViewModelProvider(this).get(ApplicationsViewModel::class.java)
        model.getData().observe(this) { results ->
            adapter.swapData(results)
        }
    }

    @SuppressLint("CheckResult")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppListBinding.inflate(inflater, container, false)
        
        val layManager = LinearLayoutManager(activity)
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    layManager.scrollToPosition(0)
                }
            }
        })
        
        binding.recycler.apply {
            layoutManager = layManager
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = this@AppListFragment.adapter
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(bars.left, bars.top, bars.right, 0)
            binding.recycler.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom)
            insets
        }

        val searchQuery = savedInstanceState?.getString(SAVE_SEARCH_QUERY)
        binding.toolbar.apply {
            title = getString(R.string.app_name) + " - " + getString(R.string.search_apps)
            setOnClickListener {
                searchMenu?.expandActionView()
            }
        }
        
        searchBinding = LayoutSearchBinding.inflate(inflater, binding.toolbar, false)
        searchBinding?.let { sb ->
            sb.searchEdit.setText(searchQuery)
            sb.searchEdit.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    model.search(sb.searchEdit.text.toString())
                    return@OnEditorActionListener true
                }
                false
            })
            sb.searchEdit.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    sb.clearBtn.visibility = if (p0.toString().isNotEmpty()) View.VISIBLE else View.GONE
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            })
            
            sb.clearBtn.setOnClickListener {
                sb.searchEdit.setText("")
                sb.searchEdit.requestFocus()
                model.search("")
                showKeyboard()
            }
        }

        binding.filterSort.setOnClickListener { onFilterItemClick.onItemClick(FilterType.SORT) }
        binding.filterType.setOnClickListener { onFilterItemClick.onItemClick(FilterType.TYPE) }
        binding.filterInstaller.setOnClickListener { onFilterItemClick.onItemClick(FilterType.INSTALLER) }
        binding.filterTargetSdk.setOnClickListener { onFilterItemClick.onItemClick(FilterType.TARGET_SDK) }

        searchMenu = binding.toolbar.menu.add(R.string.search)
            .setIcon(R.drawable.ic_search_white_24dp)
            .setActionView(searchBinding?.root)
            .setVisible(false)
            .setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                    searchBinding?.searchEdit?.requestFocus()
                    searchBinding?.searchEdit?.post { showKeyboard() }
                    return true
                }
                override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                    searchBinding?.searchEdit?.clearFocus()
                    searchBinding?.searchEdit?.setText("")
                    model.search("")
                    return true
                }
            })
        searchMenu?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
        
        if (!searchQuery.isNullOrEmpty()) {
            searchMenu?.expandActionView()
        }

        binding.toolbar.menu.add(R.string.app_theme).setOnMenuItemClickListener {
            context?.let { ctx ->
                val options: Array<CharSequence> = arrayOf(
                    resources.getString(R.string.app_theme_auto),
                    resources.getString(R.string.app_theme_light),
                    resources.getString(R.string.app_theme_dark)
                )
                MaterialAlertDialogBuilder(ctx)
                    .setTitle(R.string.app_theme)
                    .setSingleChoiceItems(options, Prefs(ctx).appTheme) { dialogInterface, i ->
                        Prefs(ctx).appTheme = i
                        Themes.setupTheme(ctx)
                        dialogInterface.dismiss()
                    }
                    .show()
            }
            true
        }.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

        binding.toolbar.menu.add(R.string.about).setOnMenuItemClickListener {
            context?.let { ctx ->
                MaterialAlertDialogBuilder(ctx)
                    .setTitle(R.string.about_app)
                    .setMessage(resources.getString(R.string.about_desc, BuildConfig.APP_VERSION_NAME))
                    .setPositiveButton(R.string.label_ok, null)
                    .setNegativeButton(R.string.about_open) { _, _ ->
                        IntentHelper.openInBrowser(ctx, "https://twitter.com/kenumir")
                    }
                    .show()
            }
            true
        }.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

        if (BuildConfig.DEBUG) {
            binding.toolbar.menu.add("Test error").setOnMenuItemClickListener {
                throw RuntimeException("Test error")
            }
        }

        setFragmentResultListener("filter") { _, result ->
            when(result.getInt("filter_type", FilterType.SORT.ordinal)) {
                FilterType.SORT.ordinal -> {
                    val order = when(result.getInt("result", 0)) {
                        1 -> ListSortOrder.DATE
                        2 -> ListSortOrder.PACKAGE
                        else -> ListSortOrder.NAME
                    }
                    model.showSortOrder(order)
                    activity?.let { Prefs(it).listSortOrder = order }
                }
                FilterType.TYPE.ordinal -> {
                    val appType = when(result.getInt("result", 0)) {
                        1 -> FilterAppType.USER
                        2 -> FilterAppType.DEBUG
                        3 -> FilterAppType.SYSTEM
                        else -> FilterAppType.ALL
                    }
                    model.setAppType(appType)
                    activity?.let { Prefs(it).listFilterAppType = appType }
                }
                FilterType.INSTALLER.ordinal -> {
                    val installer = when(result.getInt("result", 0)) {
                        1 -> FilterInstaller.PLAY_STORE
                        2 -> FilterInstaller.APP_GALLERY
                        3 -> FilterInstaller.EMPTY
                        else -> FilterInstaller.ALL
                    }
                    model.setAppInstaller(installer)
                    activity?.let { Prefs(it).listFilterInstaller = installer }
                }
                FilterType.TARGET_SDK.ordinal -> {
                    val ts = when(result.getInt("result", 0)) {
                        1 -> FilterTargetSdk.API_36
                        2 -> FilterTargetSdk.API_35
                        3 -> FilterTargetSdk.API_34
                        4 -> FilterTargetSdk.API_33
                        else -> FilterTargetSdk.ALL
                    }
                    model.setTargetSdk(ts)
                    activity?.let { Prefs(it).listFilterTargetSdk = ts }
                }
            }
            updateFilters()
        }

        if (Utils.isTV(binding.root.context)) {
            binding.appBar.visibility = View.GONE
        }

        updateFilters()
        return binding.root
    }

    private fun updateFilters() {
        context?.let { ctx ->
            binding.filterSort.text = getString(R.string.filter_placeholder, getString(R.string.sort),
                when (Prefs(ctx).listSortOrder) {
                    ListSortOrder.DATE -> getString(R.string.sort_date)
                    ListSortOrder.PACKAGE -> getString(R.string.sort_package)
                    ListSortOrder.NAME -> getString(R.string.sort_name)
                }
            )
            binding.filterType.text = getString(R.string.filter_placeholder, getString(R.string.type),
                when (Prefs(ctx).listFilterAppType) {
                    FilterAppType.ALL -> getString(R.string.type_all)
                    FilterAppType.SYSTEM -> getString(R.string.type_system)
                    FilterAppType.DEBUG -> getString(R.string.type_debug)
                    FilterAppType.USER -> getString(R.string.type_user)
                }
            )
            binding.filterInstaller.text = getString(R.string.filter_placeholder, getString(R.string.installer),
                when (Prefs(ctx).listFilterInstaller) {
                    FilterInstaller.ALL -> getString(R.string.installer_all)
                    FilterInstaller.PLAY_STORE -> getString(R.string.installer_vending)
                    FilterInstaller.APP_GALLERY -> getString(R.string.installer_huawei)
                    FilterInstaller.EMPTY -> getString(R.string.installer_empty)
                }
            )
            binding.filterTargetSdk.text = getString(R.string.filter_placeholder, getString(R.string.filter_target_sdk),
                when (Prefs(ctx).listFilterTargetSdk) {
                    FilterTargetSdk.ALL -> getString(R.string.filter_target_sdk_all)
                    FilterTargetSdk.API_36 -> getString(R.string.filter_target_sdk_36)
                    FilterTargetSdk.API_35 -> getString(R.string.filter_target_sdk_35)
                    FilterTargetSdk.API_34 -> getString(R.string.filter_target_sdk_34)
                    FilterTargetSdk.API_33 -> getString(R.string.filter_target_sdk_33)
                }
            )
        }
    }

    override fun onDestroyView() {
        parentFragmentManager.clearFragmentResultListener("filter")
        super.onDestroyView()
        _binding = null
        searchBinding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        searchBinding?.let {
            outState.putString(SAVE_SEARCH_QUERY, it.searchEdit.text.toString())
        }
        super.onSaveInstanceState(outState)
    }

    fun onBackAction(): Boolean {
        searchMenu?.let {
            if (it.isActionViewExpanded) {
                it.collapseActionView()
                return true
            }
        }
        return false
    }

    private fun showKeyboard() {
        activity?.let {
            val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            searchBinding?.searchEdit?.let { edit ->
                imm.showSoftInput(edit, 0)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AppListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
