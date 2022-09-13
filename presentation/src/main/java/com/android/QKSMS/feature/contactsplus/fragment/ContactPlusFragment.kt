package com.android.QKSMS.feature.contactsplus.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.android.QKSMS.R
import com.android.QKSMS.common.widget.SideBarView
import com.android.QKSMS.feature.compose.editing.ComposeItemPlusAdapter
import com.android.QKSMS.feature.contactsplus.ContactsPlusState
import com.android.QKSMS.filter.ContactFilter
import com.jakewharton.rxbinding2.widget.textChanges
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import javax.inject.Inject

class ContactPlusFragment (
        private var contactsAdapter: ComposeItemPlusAdapter,
        private val flag: Boolean
) : Fragment(), ContactsPlusFragmentView {
    private lateinit var recyclerView: RecyclerView
    private lateinit var search: EditText

    @Inject lateinit var contactFilter: ContactFilter

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[ContactsPlusFragmentViewModel::class.java] }

    override val queryChangedIntent: Observable<CharSequence> by lazy { search.textChanges() }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.contacts_plus_contact_fragment, container, false)
        search = view.findViewById(R.id.search)
        val visibility = if (flag) View.VISIBLE else View.GONE

        view.findViewById<FrameLayout>(R.id.container_search).visibility = visibility
        view.findViewById<SideBarView>(R.id.sidebar).visibility = visibility

        recyclerView = view.findViewById(R.id.contacts)
        recyclerView.adapter = contactsAdapter

        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height: Int = displayMetrics.heightPixels
        val params: ViewGroup.LayoutParams = recyclerView.layoutParams
        params.height = height - 600
        recyclerView.layoutParams = params
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bindView(this)
    }

    override fun render(state: ContactsPlusState) {
        if (flag) {
            contactsAdapter.data = state.contact
            view?.findViewById<SideBarView>(R.id.sidebar)?.setRecyclerView(recyclerView)
        }
    }
}