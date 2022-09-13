package com.android.QKSMS.feature.compose.editing

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android.QKSMS.common.base.QkAdapter
import com.android.QKSMS.common.base.QkViewHolder
import com.android.QKSMS.common.util.Colors
import com.android.QKSMS.common.util.Utils
import com.android.QKSMS.common.util.extensions.forwardTouches
import com.android.QKSMS.common.util.extensions.setTint
import com.android.QKSMS.databinding.ContactPlusListItemBinding
import com.android.QKSMS.extensions.associateByNotNull
import com.android.QKSMS.model.Contact
import com.android.QKSMS.model.ContactGroup
import com.android.QKSMS.model.Conversation
import com.android.QKSMS.model.Recipient
import com.android.QKSMS.repository.ConversationRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ComposeItemPlusAdapter @Inject constructor(
//        private val colors: Colors,
        private val conversationRepo: ConversationRepository
) : QkAdapter<ComposeItem, ContactPlusListItemBinding>() {

    val clicks: Subject<ComposeItem> = PublishSubject.create()
    val longClicks: Subject<ComposeItem> = PublishSubject.create()

    private val numbersViewPool = RecyclerView.RecycledViewPool()
    private val disposables = CompositeDisposable()

    var recipients: Map<String, Recipient> = mapOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder<ContactPlusListItemBinding> {
        return QkViewHolder(parent, ContactPlusListItemBinding::inflate).apply {
//            binding.icon.setTint(colors.theme().theme)

//            binding.numbers.setRecycledViewPool(numbersViewPool)
            binding.numbers.adapter = PhoneNumberPlusAdapter()
//            binding.numbers.forwardTouches(binding.root)

            binding.root.setOnClickListener {
                val item = getItem(adapterPosition)
                binding.numbers.findViewHolderForPosition(0)?.itemView?.performClick()
                binding.check.isChecked = !binding.check.isChecked
                item.getContacts().associate {contact ->
                    val address = contact.getDefaultNumber()?.address ?: contact.numbers[0]!!.address
                    if (binding.check.isChecked) {
                        Utils.selectedContacts[address] = contact.lookupKey
                    } else {
                        Utils.selectedContacts.remove(address)
                    }
                    address to contact.lookupKey
                }
                clicks.onNext(item)
            }
            binding.root.setOnLongClickListener {
                val item = getItem(adapterPosition)
                longClicks.onNext(item)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder<ContactPlusListItemBinding>, position: Int) {
        val prevItem = if (position > 0) getItem(position - 1) else null
        val item = getItem(position)

        when (item) {
            is ComposeItem.New -> bindNew(holder, item.value)
            is ComposeItem.Recent -> bindRecent(holder, item.value, prevItem)
            is ComposeItem.Starred -> bindStarred(holder, item.value, prevItem)
            is ComposeItem.Person -> bindPerson(holder, item.value, prevItem)
            is ComposeItem.Group -> bindGroup(holder, item.value, prevItem)
        }
    }

    private fun bindNew(holder: QkViewHolder<ContactPlusListItemBinding>, contact: Contact) {
        holder.binding.index.isVisible = false

        holder.binding.icon.isVisible = false

        holder.binding.avatar.recipients = listOf(createRecipient(contact))

        holder.binding.title.text = contact.numbers.joinToString { it.address }

        holder.binding.subtitle.isVisible = false

        holder.binding.numbers.isVisible = false
    }

    private fun bindRecent(holder: QkViewHolder<ContactPlusListItemBinding>, conversation: Conversation, prev: ComposeItem?) {
//        holder.binding.index.isVisible = false

//        holder.binding.icon.isVisible = prev !is ComposeItem.Recent
//        holder.binding.icon.setImageResource(R.drawable.ic_history_black_24dp)
        holder.binding.icon.visibility = View.GONE

        holder.binding.avatar.recipients = conversation.recipients

        holder.binding.title.text = conversation.getTitle()

        holder.binding.subtitle.isVisible = conversation.recipients.size > 1 && conversation.name.isBlank()
        holder.binding.subtitle.text = conversation.recipients.joinToString(", ") { recipient ->
            recipient.contact?.name ?: recipient.address
        }
        holder.binding.subtitle.collapseEnabled = conversation.recipients.size > 1

        holder.binding.numbers.isVisible = conversation.recipients.size == 1
        var adr = ""
        (holder.binding.numbers.adapter as PhoneNumberPlusAdapter).data = conversation.recipients
                .mapNotNull { recipient ->
                    run {
                        adr = recipient.address
                        recipient.contact
                    }
                }
                .flatMap { contact ->
                    run {
                        (holder.binding.numbers.adapter as PhoneNumberPlusAdapter).lookup = contact.lookupKey
                        contact.numbers.filter {
                            it.address.filter { str -> str.isDigit() } == adr.filter { str -> str.isDigit() }
                        }
                    }
                }

//        holder.binding.divider.isVisible = true
        holder.binding.check.isVisible = true// (holder.binding.numbers.adapter as PhoneNumberPlusAdapter).data.isEmpty()
    }

    private fun bindStarred(holder: QkViewHolder<ContactPlusListItemBinding>, contact: Contact, prev: ComposeItem?) {
        holder.binding.index.isVisible = true
        holder.binding.index.text = if (contact.name.getOrNull(0)?.isLetter() == true) contact.name[0].toString() else "#"
        holder.binding.index.isVisible = prev !is ComposeItem.Person ||
                (contact.name[0].isLetter() && !contact.name[0].equals(prev.value.name[0], ignoreCase = true)) ||
                (!contact.name[0].isLetter() && prev.value.name[0].isLetter())

        holder.binding.icon.isVisible = false
//        holder.binding.index.isVisible = false

//        holder.binding.icon.isVisible = prev !is ComposeItem.Starred
//        holder.binding.icon.setImageResource(R.drawable.ic_star_black_24dp)

        holder.binding.avatar.recipients = listOf(createRecipient(contact))

        holder.binding.title.text = contact.name

        holder.binding.subtitle.isVisible = false

        holder.binding.numbers.isVisible = true
        (holder.binding.numbers.adapter as PhoneNumberPlusAdapter).data = contact.numbers
        (holder.binding.numbers.adapter as PhoneNumberPlusAdapter).lookup = contact.lookupKey
    }

    private fun bindGroup(holder: QkViewHolder<ContactPlusListItemBinding>, group: ContactGroup, prev: ComposeItem?) {
        holder.binding.index.isVisible = false

        holder.binding.icon.isVisible = prev !is ComposeItem.Group
//        holder.binding.icon.setImageResource(R.drawable.ic_people_black_24dp)

        holder.binding.avatar.recipients = group.contacts.map(::createRecipient)

        holder.binding.title.text = group.title

        holder.binding.subtitle.isVisible = true
        holder.binding.subtitle.text = group.contacts.joinToString(", ") { it.name }
        holder.binding.subtitle.collapseEnabled = group.contacts.size > 1

        holder.binding.numbers.isVisible = false
    }

    private fun bindPerson(holder: QkViewHolder<ContactPlusListItemBinding>, contact: Contact, prev: ComposeItem?) {
        holder.binding.index.isVisible = true
        holder.binding.index.text = if (contact.name.getOrNull(0)?.isLetter() == true) contact.name[0].toString() else "#"
        holder.binding.index.isVisible = prev !is ComposeItem.Person ||
                (contact.name[0].isLetter() && !contact.name[0].equals(prev.value.name[0], ignoreCase = true)) ||
                (!contact.name[0].isLetter() && prev.value.name[0].isLetter())

        holder.binding.icon.isVisible = false

        holder.binding.avatar.recipients = listOf(createRecipient(contact))

        holder.binding.title.text = contact.name

        holder.binding.subtitle.isVisible = false

        holder.binding.numbers.isVisible = true
        holder.binding.check.isVisible = contact.numbers.size > 0
        (holder.binding.numbers.adapter as PhoneNumberPlusAdapter).data = contact.numbers
        (holder.binding.numbers.adapter as PhoneNumberPlusAdapter).lookup = contact.lookupKey
    }

    private fun createRecipient(contact: Contact): Recipient {
        return recipients[contact.lookupKey] ?: Recipient(
                address = contact.numbers.firstOrNull()?.address ?: "",
                contact = contact)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        disposables += conversationRepo.getUnmanagedRecipients()
                .map { recipients -> recipients.associateByNotNull { recipient -> recipient.contact?.lookupKey } }
                .subscribe { recipients -> this@ComposeItemPlusAdapter.recipients = recipients }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        disposables.clear()
    }

    override fun areItemsTheSame(old: ComposeItem, new: ComposeItem): Boolean {
        val oldIds = old.getContacts().map { contact -> contact.lookupKey }
        val newIds = new.getContacts().map { contact -> contact.lookupKey }
        return oldIds == newIds
    }

    override fun areContentsTheSame(old: ComposeItem, new: ComposeItem): Boolean {
        return false
    }

}
