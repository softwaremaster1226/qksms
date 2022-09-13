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
package com.android.QKSMS.feature.scheduled

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import com.android.QKSMS.common.base.QkAdapter
import com.android.QKSMS.common.base.QkViewHolder
import com.android.QKSMS.databinding.ScheduledMessageImageListItemBinding
import com.android.QKSMS.util.GlideApp
import javax.inject.Inject

class ScheduledMessageAttachmentAdapter @Inject constructor(
    private val context: Context
) : QkAdapter<Uri, ScheduledMessageImageListItemBinding>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder<ScheduledMessageImageListItemBinding> {
        return QkViewHolder(parent, ScheduledMessageImageListItemBinding::inflate).apply {
            binding.thumbnail.clipToOutline = true
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder<ScheduledMessageImageListItemBinding>, position: Int) {
        val attachment = getItem(position)

        GlideApp.with(context).load(attachment).into(holder.binding.thumbnail)
    }

}
