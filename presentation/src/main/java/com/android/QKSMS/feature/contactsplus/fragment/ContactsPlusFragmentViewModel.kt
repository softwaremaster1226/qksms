package com.android.QKSMS.feature.contactsplus.fragment

import android.content.Context
import com.android.QKSMS.common.base.QkViewModel
import com.android.QKSMS.extensions.removeAccents
import com.android.QKSMS.feature.compose.editing.ComposeItem
import com.android.QKSMS.feature.contactsplus.ContactsPlusState
import com.android.QKSMS.filter.ContactFilter
import com.android.QKSMS.model.*
import com.android.QKSMS.repository.ContactRepository
import com.android.QKSMS.util.PhoneNumberUtils
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import io.realm.RealmList
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ContactsPlusFragmentViewModel @Inject constructor(
        private val context: Context,
        private val contactsRepo: ContactRepository,
        private val contactFilter: ContactFilter,
        private val phoneNumberUtils: PhoneNumberUtils
) : QkViewModel<ContactsPlusFragmentView, ContactsPlusState>(ContactsPlusState()) {
    private val contacts: Observable<List<Contact>> by lazy { contactsRepo.getUnmanagedContacts() }
    
    override fun bindView(view: ContactsPlusFragmentView) {
        super.bindView(view)

        Observables
                .combineLatest(
                        view.queryChangedIntent, contacts
                ) { query, contacts ->
                    val composeItems = mutableListOf<ComposeItem>()
                    if (query.isBlank()) {

                        composeItems += contacts
                                .map(ComposeItem::Person)
                    } else {

                        // Strip the accents from the query. This can be an expensive operation, so
                        // cache the result instead of doing it for each contact
                        val normalizedQuery = query.removeAccents()

                        composeItems += contacts
                                .asSequence()
                                .filter { contact -> contactFilter.filter(contact, normalizedQuery) }
                                .map(ComposeItem::Person)
                    }

                    composeItems
                }
                .debounce(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .autoDisposable(view.scope())
                .subscribe { items -> newState { copy(contact = items) } }
    }
}