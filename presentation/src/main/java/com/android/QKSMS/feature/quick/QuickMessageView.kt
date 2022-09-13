package com.android.QKSMS.feature.quick

import androidx.recyclerview.widget.RecyclerView
import com.android.QKSMS.common.base.QkView
import io.reactivex.Observable

interface QuickMessageView: QkView<QuickMessageState> {
    val addIntent: Observable<*>
    val adapter: RecyclerView.Adapter<*>

    fun addQM()
}

interface QuickMessageClick {
    fun onClick(data: String)
}