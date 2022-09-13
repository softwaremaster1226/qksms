package com.android.QKSMS.feature.calendarevent

import com.android.QKSMS.common.util.Utils

interface CalendarEventAction {
    fun done()
    fun insertToSelected(data: Utils.CalendarEventDetail)
}