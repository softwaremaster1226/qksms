package com.android.QKSMS.feature.settings.ringtone

import com.android.QKSMS.common.base.QkViewModel
import javax.inject.Inject

class RingtoneViewModel @Inject constructor() : QkViewModel<RingtoneView, RingtoneState>(RingtoneState()) {
    override fun bindView(view: RingtoneView) {
        super.bindView(view)
    }
}