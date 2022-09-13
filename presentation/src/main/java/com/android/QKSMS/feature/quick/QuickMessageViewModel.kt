package com.android.QKSMS.feature.quick

import android.content.Context
import com.android.QKSMS.common.base.QkViewModel
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import javax.inject.Inject

class QuickMessageViewModel @Inject constructor(
        private val context: Context
) : QkViewModel<QuickMessageView, QuickMessageState>(QuickMessageState()) {
    override fun bindView(view: QuickMessageView) {
        super.bindView(view)
        view.addIntent
                .autoDisposable(view.scope())
                .subscribe {
                    view.addQM()
                }
    }
}