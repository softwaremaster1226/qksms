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
package com.android.QKSMS.feature.compose.part

import android.content.Context
import com.android.QKSMS.common.base.QkViewHolder
import com.android.QKSMS.common.util.Colors
import com.android.QKSMS.common.util.extensions.setVisible
import com.android.QKSMS.common.widget.BubbleImageView
import com.android.QKSMS.databinding.MmsPreviewListItemBinding
import com.android.QKSMS.extensions.isImage
import com.android.QKSMS.extensions.isVideo
import com.android.QKSMS.model.Message
import com.android.QKSMS.model.MmsPart
import com.android.QKSMS.util.GlideApp
import javax.inject.Inject

class MediaBinder @Inject constructor(
    colors: Colors,
    private val context: Context
) : PartBinder<MmsPreviewListItemBinding>(MmsPreviewListItemBinding::inflate) {

    override var theme = colors.theme()

    override fun canBindPart(part: MmsPart) = part.isImage() || part.isVideo()

    override fun bindPartInternal(
        holder: QkViewHolder<MmsPreviewListItemBinding>,
        part: MmsPart,
        message: Message,
        canGroupWithPrevious: Boolean,
        canGroupWithNext: Boolean
    ) {
        holder.binding.video.setVisible(part.isVideo())
        holder.binding.root.setOnClickListener { clicks.onNext(part.id) }

        holder.binding.thumbnail.bubbleStyle = when {
            !canGroupWithPrevious && canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_FIRST else BubbleImageView.Style.IN_FIRST
            canGroupWithPrevious && canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_MIDDLE else BubbleImageView.Style.IN_MIDDLE
            canGroupWithPrevious && !canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_LAST else BubbleImageView.Style.IN_LAST
            else -> BubbleImageView.Style.ONLY
        }

        GlideApp.with(context).load(part.getUri()).fitCenter().into(holder.binding.thumbnail)
    }

}