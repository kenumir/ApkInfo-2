package com.wt.apkinfo.fragments


import android.content.Context
import android.os.Build
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.appbar.MaterialToolbar
import com.wt.apkinfo.BuildConfig
import com.wt.apkinfo.R
import com.wt.apkinfo.activities.AppDetailsActivity
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.models.ApplicationsViewModel
import com.wt.apkinfo.proto.AppListAdapter
import com.wt.apkinfo.proto.IntentHelper
import com.wt.apkinfo.proto.OnAppListItemClick


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
            activity?.let { AppDetailsActivity.show(it, item) }
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
            .observe(this, Observer { results ->
                adapter.swapData(results)
            })
    }

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
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                it.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        it.context,
                        R.drawable.ic_search_suggest
                    ), null, null, null
                )
            }
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
            .setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                    searchEdit?.requestFocus()
                    searchEdit?.post {
                        showKeyboard()
                    }
                    return true
                }
                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
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

        toolbar.menu.add(R.string.about)
            .setOnMenuItemClickListener {
                context?.let {
                    MaterialDialog(it).show {
                        title(R.string.about_app)
                        message(text = resources.getString(
                            R.string.about_desc,
                            BuildConfig.VERSION_NAME
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
                true
            }
        }
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SAVE_SEARCH_QUERY, searchEdit?.text.toString())
        super.onSaveInstanceState(outState)
    }

    fun onBackAction(): Boolean {
        searchEdit?.let {
            if (it.text.toString().isNotEmpty()) {
                searchMenu?.collapseActionView()
                return true
            }
            return false
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
