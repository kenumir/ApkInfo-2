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
import android.widget.ImageView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wt.apkinfo.activities.AppDetailsActivity
import com.wt.apkinfo.base.BuildConfig
import com.wt.apkinfo.base.R
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.Prefs
import com.wt.apkinfo.data.models.ApplicationsViewModel
import com.wt.apkinfo.proto.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val SAVE_SEARCH_QUERY = "search_query"

/**
 * A simple [Fragment] subclass.
 * Use the [AppListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AppListFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private var recycler: RecyclerView? = null
    private var adapter: AppListAdapter = AppListAdapter(object : OnAppListItemClick {
        override fun onItemClick(item: ApplicationEntryInfo?) {
            if (item?.pkg == null) {
                // skip action
            } else {
                activity?.let { AppDetailsActivity.show(it, item) }
            }
        }
    }, object: OnFilterItemClick {
        override fun onItemClick(item: FilterType) {
            val titleRes = when (item) {
                FilterType.SORT -> R.string.sort
                FilterType.TYPE -> R.string.type
                FilterType.INSTALLER -> R.string.installer
            }
            val itemsRes = when (item) {
                FilterType.SORT -> intArrayOf(R.string.sort_name, R.string.sort_date, R.string.sort_package)
                FilterType.TYPE -> intArrayOf(R.string.type_all, R.string.type_user, R.string.type_debug, R.string.type_system)
                FilterType.INSTALLER -> intArrayOf(R.string.installer_all, R.string.installer_vending, R.string.installer_huawei, R.string.installer_empty)
            }
            val selPos = activity?.let {
                when (item) {
                    FilterType.SORT -> when (Prefs(it).listSortOrder) {
                        ListSortOrder.DATE -> 1
                        ListSortOrder.PACKAGE -> 2
                        else -> 0
                    }
                    FilterType.TYPE -> //if (Prefs(it).allApps == 1) 0 else 1
                        when (Prefs(it).listFilterAppType) {
                            FilterAppType.ALL -> 0
                            FilterAppType.USER -> 1
                            FilterAppType.DEBUG -> 2
                            FilterAppType.SYSTEM -> 3
                        }
                    FilterType.INSTALLER ->
                        when (Prefs(it).listFilterInstaller) {
                            FilterInstaller.ALL -> 0
                            FilterInstaller.PLAY_STORE -> 1
                            FilterInstaller.APP_GALLERY -> 2
                            FilterInstaller.EMPTY -> 3
                        }
                }

            } ?: 0
            val modalBottomSheet = FiltersFragment.create(
                titleRes,
                itemsRes,
                selPos,
                item
            )
            modalBottomSheet.show(parentFragmentManager, FiltersFragment.TAG)
        }
    }, object: OnAdapterFilterData {
        override fun getSort(): ListSortOrder {
            return activity?.let {
                model.listSortOrder
                //Prefs(it).listSortOrder
            } ?: ListSortOrder.NAME
        }
        override fun getAppType(): FilterAppType {
            return activity?.let { a ->
                model.filterAppType
                //Prefs(a).listFilterAppType
            } ?: run {
                FilterAppType.ALL
            }
        }
        override fun getAppInstaller(): FilterInstaller {
            return activity?.let { a ->
                model.filterInstaller
                //Prefs(a).listFilterInstaller
            } ?: run {
                FilterInstaller.ALL
            }
        }
    })
    private var searchEdit: AppCompatAutoCompleteTextView? = null
    private lateinit var model: ApplicationsViewModel
    private var searchMenu: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        model = ViewModelProvider(this).get(ApplicationsViewModel::class.java)
        model.getData()
            .observe(this) { results ->
                adapter.swapData(results)
            }
    }

    @SuppressLint("CheckResult")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_app_list, container, false)
        val layManager = LinearLayoutManager(activity)
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    layManager.scrollToPosition(0)
                }
            }
        })
        recycler = view.findViewById(R.id.recycler)
        recycler?.layoutManager = layManager
        recycler?.itemAnimator = DefaultItemAnimator()
        recycler?.setHasFixedSize(true)
        recycler?.adapter = adapter

        val searchQuery = savedInstanceState?.getString(SAVE_SEARCH_QUERY)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar).apply {
            title = getString(R.string.app_name) + " - " + getString(R.string.search_apps)
            setOnClickListener {
                searchMenu?.expandActionView()
            }
        }
        val searchView = inflater.inflate(R.layout.layout_search, toolbar, false)
        val clearBtn = searchView.findViewById<ImageView>(R.id.clearBtn)
        searchEdit = searchView.findViewById(R.id.searchEdit)
        searchEdit?.let {
            it.setText(searchQuery)
            it.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    model.search(it.text.toString())
                    return@OnEditorActionListener true
                }
                false
            })
            it.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    clearBtn.visibility = if (p0.toString().isNotEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            })
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //    it.setCompoundDrawablesWithIntrinsicBounds(
            //        ContextCompat.getDrawable(
            //            it.context,
            //            R.drawable.ic_search_suggest
            //        ), null, null, null
            //    )
            //}
        }

        clearBtn.setOnClickListener {
            searchEdit?.let {
                it.setText("")
                it.requestFocus()
            }
            model.search("")
            showKeyboard()
        }

        searchMenu = toolbar.menu.add(R.string.search)
            .setIcon(R.drawable.ic_search_white_24dp)
            .setActionView(searchView)
            .setVisible(false)
            .setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                    searchEdit?.requestFocus()
                    searchEdit?.post {
                        showKeyboard()
                    }
                    return true
                }
                override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                    searchEdit?.clearFocus()
                    searchEdit?.setText("")
                    model.search("")
                    return true
                }

            })
        searchMenu?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
        searchQuery?.let {
            if (it.isNotEmpty()) {
                searchMenu?.expandActionView()
            }
        }

        /*
        toolbar.menu.add(R.string.show_all_apps)
            .setCheckable(true)
            .setChecked(
                activity?.let { a ->
                    Prefs(a).allApps == 1
                } ?: run {
                    false
                }
            )
            .setOnMenuItemClickListener {
                it.isChecked = !it.isChecked
                model.showAllApps(it.isChecked)
                activity?.let { a ->
                    Prefs(a).allApps = if (it.isChecked) 1 else 0
                }
                true
            }
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

        toolbar.menu.add(R.string.sort)
            .setOnMenuItemClickListener {
                activity?.let {
                    val selPos = when(Prefs(it).listSortOrder) {
                        ListSortOrder.DATE -> 1
                        ListSortOrder.PACKAGE -> 2
                        else -> 0
                    }
                    MaterialDialog(it).show {
                        title(R.string.sort)
                        listItemsSingleChoice(R.array.sort_orders, initialSelection = selPos) { _, index, _ ->
                            // Invoked when the user selects an item
                            val order = when(index) {
                                1 -> ListSortOrder.DATE
                                2 -> ListSortOrder.PACKAGE
                                else -> ListSortOrder.NAME
                            }
                            model.showSortOrder(order)
                            Prefs(it).listSortOrder = order
                        }
                    }
                }

                true
            }
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

         */

        toolbar.menu.add(R.string.app_theme)
            .setOnMenuItemClickListener {
                context?.let {
                    val options: Array<CharSequence> = arrayOf(
                        resources.getString(R.string.app_theme_auto),
                        resources.getString(R.string.app_theme_light),
                        resources.getString(R.string.app_theme_dark)
                    )
                    val theme = Prefs(it).appTheme
                    MaterialAlertDialogBuilder(it)
                        .setTitle(R.string.app_theme)
                        .setSingleChoiceItems(options, theme) { dialogInterface, i ->
                            Prefs(it).appTheme = i
                            Themes.setupTheme(it)
                            dialogInterface.dismiss()
                        }
                        .show()
                }
                true
            }
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

        toolbar.menu.add(R.string.about)
            .setOnMenuItemClickListener {
                context?.let {
                    MaterialDialog(it).show {
                        title(R.string.about_app)
                        message(text = resources.getString(
                            R.string.about_desc,
                            BuildConfig.APP_VERSION_NAME
                        ))
                        positiveButton(R.string.label_ok)
                        negativeButton(R.string.about_open) {
                            IntentHelper.openInBrowser(context, "https://twitter.com/kenumir")
                        }
                    }
                }

                true
            }
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

        if (BuildConfig.DEBUG) {
            toolbar.menu.add("Test error").setOnMenuItemClickListener {
                //ERA.testError(activity)
                throw RuntimeException("Test error")
            }
        }

        setFragmentResultListener("filter") { requestKey, result ->
            if ("filter" == requestKey) {
                when(result.getInt("filter_type", FilterType.SORT.ordinal)) {
                    FilterType.SORT.ordinal -> {
                        val order = when(result.getInt("result", 0)) {
                            0 -> ListSortOrder.NAME
                            1 -> ListSortOrder.DATE
                            2 -> ListSortOrder.PACKAGE
                            else -> ListSortOrder.NAME
                        }
                        model.showSortOrder(order)
                        activity?.let {Prefs(it).listSortOrder = order}
                    }
                    FilterType.TYPE.ordinal -> {
                        val appType = when(result.getInt("result", 0)) {
                            0 -> FilterAppType.ALL
                            1 -> FilterAppType.USER
                            2 -> FilterAppType.DEBUG
                            3 -> FilterAppType.SYSTEM
                            else -> FilterAppType.ALL
                        }
                        model.setAppType(appType)
                        activity?.let { a ->
                            Prefs(a).listFilterAppType = appType
                        }
                    }
                    FilterType.INSTALLER.ordinal -> {
                        val installer = when(result.getInt("result", 0)) {
                            0 -> FilterInstaller.ALL
                            1 -> FilterInstaller.PLAY_STORE
                            2 -> FilterInstaller.APP_GALLERY
                            3 -> FilterInstaller.EMPTY
                            else -> FilterInstaller.ALL
                        }
                        model.setAppInstaller(installer)
                        activity?.let { a ->
                            Prefs(a).listFilterInstaller = installer
                        }
                    }
                }

            }
        }

        if (Utils.isTV(view.context)) {
            view.findViewById<View>(R.id.appBar).visibility = View.GONE
        }

        return view
    }

    override fun onDestroyView() {
        parentFragmentManager.clearFragmentResultListener("filter")
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SAVE_SEARCH_QUERY, searchEdit?.text.toString())
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
            imm.showSoftInput(searchEdit, 0)
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
