package com.android.QKSMS.feature.contactsplus.fragment

import com.android.QKSMS.common.base.QkView
import com.android.QKSMS.feature.contactsplus.ContactsPlusState
import io.reactivex.Observable

interface ContactsPlusFragmentView: QkView<ContactsPlusState> {
    val queryChangedIntent: Observable<CharSequence>
}