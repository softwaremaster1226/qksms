package com.android.QKSMS.feature.calendarevent

import com.android.QKSMS.common.base.QkViewModel
import javax.inject.Inject

class CalendarEventViewModel @Inject constructor() : QkViewModel<CalendarEventView, CalendarEventState>(CalendarEventState()) {
    override fun bindView(view: CalendarEventView) {
        super.bindView(view)
    }
}