package com.android.QKSMS.feature.settings.ringtone.util

import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.android.QKSMS.R
import com.android.QKSMS.common.util.SongsModel
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder


class RingtoneTitleViewHolder(itemView: View) : GroupViewHolder(itemView) {
    private val genreTitle: TextView = itemView.findViewById(R.id.title)
    private val arrow: ImageView = itemView.findViewById(R.id.arrow)
    fun setGenreTitle(group: ExpandableGroup<*>) {
        genreTitle.text = group.title
    }

    override fun expand() {
        animateExpand()
    }

    override fun collapse() {
        animateCollapse()
    }

    private fun animateExpand() {
        val rotate = RotateAnimation(360F, 180F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 300
        rotate.fillAfter = true
        arrow.animation = rotate
    }

    private fun animateCollapse() {
        val rotate = RotateAnimation(180F, 360F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 300
        rotate.fillAfter = true
        arrow.animation = rotate
    }
}

class RingtoneViewHolder(itemView: View) : ChildViewHolder(itemView) {
    private val artistName: TextView = itemView.findViewById(R.id.title)
    fun setRingtoneName(artist: SongsModel) {
        artistName.text = artist.mSongsName
        artistName.setOnClickListener {

        }
    }

}