package com.android.QKSMS.feature.contactsplus

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.android.QKSMS.common.base.QkThemedActivity
import com.android.QKSMS.common.util.Utils
import com.android.QKSMS.common.util.extensions.viewBinding
import com.android.QKSMS.databinding.ContactsPlusActivityBinding
import com.android.QKSMS.feature.compose.editing.ComposeItem
import com.android.QKSMS.feature.compose.editing.ComposeItemPlusAdapter
import com.android.QKSMS.feature.contacts.ContactsActivity
import com.android.QKSMS.feature.contactsplus.fragment.ContactPlusFragment
import com.android.QKSMS.model.Contact
import com.android.QKSMS.repository.ContactRepository
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ContactsPlusActivity() : QkThemedActivity(), ContactsPlusView {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject lateinit var contactsAdapter: ComposeItemPlusAdapter
    @Inject lateinit var recentAdapter: ComposeItemPlusAdapter
    @Inject lateinit var groupAdapter: ComposeItemPlusAdapter
    @Inject lateinit var starAdapter: ComposeItemPlusAdapter

    var contacts: Observable<List<Contact>> = Observable.just(ArrayList())

    private val binding by viewBinding(ContactsPlusActivityBinding::inflate)
    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[ContactsPlusViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        showBackButton(true)
        viewModel.bindView(this)

        binding.contacts.adapter = ViewPagerAdapter(supportFragmentManager)
        binding.tabs.setupWithViewPager(binding.contacts)
        Utils.selectedContacts.clear()

        binding.done.setOnClickListener {
            val intent = Intent().putExtra(ContactsActivity.ChipsKey, Utils.selectedContacts)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    inner class ViewPagerAdapter(fm: FragmentManager):FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return when(position) {
                0 -> ContactPlusFragment(recentAdapter, false)
                1 -> ContactPlusFragment(contactsAdapter, true)
                2 -> ContactPlusFragment(groupAdapter, false)
                else -> ContactPlusFragment(starAdapter, false)
            }
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when(position) {
                0 -> "Recent"
                1 -> "Contacts"
                2 -> "Groups"
                else -> ""
            }
        }
    }

    override fun render(state: ContactsPlusState) {
        contactsAdapter.data = state.contact
        starAdapter.data = state.starred
        groupAdapter.data = state.group
        recentAdapter.data = state.recent
    }
}