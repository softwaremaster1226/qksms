package com.android.QKSMS.feature.settings.ringtone.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.QKSMS.R
import com.android.QKSMS.common.util.SongsModel
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

class RingtoneAdapter(groups: List<ExpandableGroup<*>>) : ExpandableRecyclerViewAdapter<RingtoneTitleViewHolder, RingtoneViewHolder>(groups) {
    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): RingtoneTitleViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.ringtone_group, parent, false)
        return RingtoneTitleViewHolder(view)
    }

    override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): RingtoneViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.ringtone_child, parent, false)
        return RingtoneViewHolder(view)
    }

    override fun onBindChildViewHolder(holder: RingtoneViewHolder, flatPosition: Int, group: ExpandableGroup<*>, childIndex: Int) {
        val artist: SongsModel = (group as Ringtones).items[childIndex] as SongsModel
        holder.setRingtoneName(artist)
    }

    override fun onBindGroupViewHolder(holder: RingtoneTitleViewHolder, flatPosition: Int, group: ExpandableGroup<*>) {
        holder.setGenreTitle(group)
    }
}