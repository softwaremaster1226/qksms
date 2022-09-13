/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.android.QKSMS.feature.conversations

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import com.android.QKSMS.R
import com.android.QKSMS.common.Navigator
import com.android.QKSMS.common.base.QkRealmAdapter
import com.android.QKSMS.common.base.QkViewHolder
import com.android.QKSMS.common.util.Colors
import com.android.QKSMS.common.util.DateFormatter
import com.android.QKSMS.common.util.extensions.resolveThemeColor
import com.android.QKSMS.common.util.extensions.setTint
import com.android.QKSMS.databinding.ConversationListItemBinding
import com.android.QKSMS.model.Conversation
import com.android.QKSMS.model.Recipient
import com.android.QKSMS.util.PhoneNumberUtils
import com.android.QKSMS.util.Preferences
import javax.inject.Inject

class ConversationsAdapter @Inject constructor(
    private val colors: Colors,
    private val context: Context,
    private val dateFormatter: DateFormatter,
    private val navigator: Navigator,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val prefs: Preferences
) : QkRealmAdapter<Conversation, ConversationListItemBinding>() {

    init {
        // This is how we access the threadId for the swipe actions
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder<ConversationListItemBinding> {
        return QkViewHolder(parent, ConversationListItemBinding::inflate).apply {
            if (viewType == 1) {
                val textColorPrimary = parent.context.resolveThemeColor(android.R.attr.textColorPrimary)

                binding.title.setTypeface(binding.title.typeface, Typeface.BOLD)

                binding.snippet.setTypeface(binding.snippet.typeface, Typeface.BOLD)
                binding.snippet.setTextColor(textColorPrimary)
                binding.snippet.maxLines = 5

                binding.unread.isVisible = true

                binding.date.setTypeface(binding.date.typeface, Typeface.BOLD)
                binding.date.setTextColor(textColorPrimary)
            }

            binding.root.setOnClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnClickListener
                when (toggleSelection(conversation.id, false)) {
                    true -> binding.root.isActivated = isSelected(conversation.id)
                    false -> {
                        navigator.showConversation(conversation.id, null, conversation.locked)
                    }
                }
            }

            binding.root.setOnLongClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnLongClickListener true
                toggleSelection(conversation.id)
                binding.root.isActivated = isSelected(conversation.id)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder<ConversationListItemBinding>, position: Int) {
        val conversation = getItem(position) ?: return

        // If the last message wasn't incoming, then the colour doesn't really matter anyway
        val lastMessage = conversation.lastMessage
        val recipient = when {
            conversation.recipients.size == 1 || lastMessage == null -> conversation.recipients.firstOrNull()
            else -> conversation.recipients.find { recipient ->
                phoneNumberUtils.compare(recipient.address, lastMessage.address)
            }
        }
        val theme = colors.theme(recipient).theme

        holder.binding.root.isActivated = isSelected(conversation.id)

        if(!prefs.showAvatar.get()) {
            holder.binding.avatars.recipients = conversation.recipients
        } else {
            holder.binding.avatars.recipients = ArrayList<Recipient>()
        }
        holder.binding.title.collapseEnabled = conversation.recipients.size > 1
        holder.binding.title.text = buildSpannedString {
            append(conversation.getTitle())
            if (conversation.draft.isNotEmpty()) {
                color(theme) { append(" " + context.getString(R.string.main_draft)) }
            }
        }
        holder.binding.date.text = conversation.date.takeIf { it > 0 }?.let(dateFormatter::getConversationTimestamp)
        holder.binding.snippet.text = when {
            conversation.draft.isNotEmpty() -> conversation.draft
            conversation.locked -> "Locked!"
            conversation.me -> context.getString(R.string.main_sender_you, conversation.snippet)
            else -> conversation.snippet
        }
        holder.binding.pinned.isVisible = conversation.pinned
        holder.binding.unread.setTint(theme)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: -1
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.unread == false) 0 else 1
    }
}
