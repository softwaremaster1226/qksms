package com.android.QKSMS.feature.settings.ringtone.util

import com.android.QKSMS.common.util.SongsModel
import com.thoughtbot.expandablecheckrecyclerview.models.SingleCheckExpandableGroup

class Ringtones(title: String, items: List<SongsModel>) : SingleCheckExpandableGroup(title, items) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ringtones) return false
        return false
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

}