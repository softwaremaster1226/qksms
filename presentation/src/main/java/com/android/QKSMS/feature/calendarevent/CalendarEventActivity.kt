package com.android.QKSMS.feature.calendarevent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.QKSMS.common.base.QkThemedActivity
import com.android.QKSMS.common.util.Utils
import com.android.QKSMS.common.util.extensions.viewBinding
import com.android.QKSMS.databinding.CalendarEventActivityBinding
import dagger.android.AndroidInjection
import javax.inject.Inject

class CalendarEventActivity : QkThemedActivity(), CalendarEventView, CalendarEventAction {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val binding by viewBinding(CalendarEventActivityBinding::inflate)
    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[CalendarEventViewModel::class.java] }

    private val selected = ArrayList<Utils.CalendarEventDetail>()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel.bindView(this)
        binding.content.layoutManager = LinearLayoutManager(this)

        val adapter = ContentsAdapter(this, Utils.readCalendarEvent(applicationContext))

        binding.content.adapter = adapter
        binding.description.isVisible = adapter.itemCount == 0
        binding.done.setOnClickListener {
            done()
        }
    }

    override fun render(state: CalendarEventState) {

    }

    override fun done() {
        val intent = Intent()
        intent.putExtra("data", selected)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun insertToSelected(data: Utils.CalendarEventDetail) {
        selected.remove(data)
        selected.add(data)
    }
}