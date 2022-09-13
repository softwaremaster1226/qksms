package com.android.QKSMS.feature.contactsplus

import android.content.Context
import com.android.QKSMS.common.base.QkViewModel
import com.android.QKSMS.feature.compose.editing.ComposeItem
import com.android.QKSMS.model.Contact
import com.android.QKSMS.model.ContactGroup
import com.android.QKSMS.model.Conversation
import com.android.QKSMS.model.Recipient
import com.android.QKSMS.repository.ContactRepository
import com.android.QKSMS.repository.ConversationRepository
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ContactsPlusViewModel @Inject constructor(
        private val context: Context,
        serializedChips: HashMap<String, String?>,
        private val contactsRepo: ContactRepository,
        private val conversationRepo: ConversationRepository
) : QkViewModel<ContactsPlusView, ContactsPlusState>(ContactsPlusState()) {
    private val contactGroups: Observable<List<ContactGroup>> by lazy { contactsRepo.getUnmanagedContactGroups() }
    private val contacts: Observable<List<Contact>> by lazy { contactsRepo.getUnmanagedContacts() }
    private val recents: Observable<List<Conversation>> by lazy {
        conversationRepo.getUnmanagedConversations()
//        if (sharing) conversationRepo.getUnmanagedConversations() else Observable.just(listOf())
    }
    private val starredContacts: Observable<List<Contact>> by lazy { contactsRepo.getUnmanagedContacts(true) }

    private val selectedChips = Observable.just(serializedChips)
            .observeOn(Schedulers.io())
            .map { hashmap ->
                hashmap.map { (address, lookupKey) ->
                    Recipient(address = address, contact = lookupKey?.let(contactsRepo::getUnmanagedContact))
                }
            }

    override fun bindView(view: ContactsPlusView) {
        super.bindView(view)
        Observables.combineLatest(
                recents, selectedChips
        ) { recents, selectedChips ->
            val composeItems = mutableListOf<ComposeItem>()

            composeItems += recents
                    .filter { conversation ->
                        conversation.recipients.any { recipient ->
                            selectedChips.none { chip ->
                                if (recipient.contact == null) {
                                    chip.address == recipient.address
                                } else {
                                    chip.contact?.lookupKey == recipient.contact?.lookupKey
                                }
                            }
                        }
                    }
                    .map(ComposeItem::Recent)
            composeItems
        }
                .subscribeOn(Schedulers.computation())
                .autoDisposable(view.scope())
                .subscribe { items -> newState { copy(recent = items) } }

        Observables.combineLatest(
                starredContacts, selectedChips
        ) { starredContacts, selectedChips ->
            val composeItems = mutableListOf<ComposeItem>()
            composeItems += starredContacts
                    .filter { contact -> selectedChips.none { it.contact?.lookupKey == contact.lookupKey } }
                    .map(ComposeItem::Starred)
            composeItems
        }
                .subscribeOn(Schedulers.computation())
                .autoDisposable(view.scope())
                .subscribe { items -> newState { copy(starred = items) } }

        Observables.combineLatest(
                contactGroups, selectedChips
        ) { contactGroups, selectedChips ->
            val composeItems = mutableListOf<ComposeItem>()

            composeItems += contactGroups
                    .filter { group ->
                        group.contacts.any { contact ->
                            selectedChips.none { chip -> chip.contact?.lookupKey == contact.lookupKey }
                        }
                    }
                    .map(ComposeItem::Group)
            composeItems
        }
                .subscribeOn(Schedulers.computation())
                .autoDisposable(view.scope())
                .subscribe { items -> newState { copy(group = items) } }

        Observables.combineLatest(
                contacts, selectedChips
        ) { contacts, selectedChips ->
            val composeItems = mutableListOf<ComposeItem>()
            composeItems += contacts
                    .filter { contact -> selectedChips.none { it.contact?.lookupKey == contact.lookupKey } }
                    .map(ComposeItem::Person)
            composeItems
        }
                .subscribeOn(Schedulers.computation())
                .autoDisposable(view.scope())
                .subscribe { items -> newState { copy(contact = items) } }
    }
}