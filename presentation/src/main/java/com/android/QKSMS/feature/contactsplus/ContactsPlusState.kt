package com.android.QKSMS.feature.contactsplus

import com.android.QKSMS.feature.compose.editing.ComposeItem
import com.android.QKSMS.model.Contact
import io.reactivex.Observable

data class ContactsPlusState(
        val recent: List<ComposeItem> = ArrayList(),
        val starred: List<ComposeItem> = ArrayList(),
        val contact: List<ComposeItem> = ArrayList(),
        val group: List<ComposeItem> = ArrayList()
)