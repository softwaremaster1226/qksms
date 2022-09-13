package com.android.QKSMS.feature.calendarevent

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.QKSMS.R
import com.android.QKSMS.common.util.Utils

class ContentsAdapter(private val mContext: Context, data: ArrayList<Utils.CalendarEventData>): RecyclerView.Adapter<ContentsAdapter.ViewHolder>() {
    private val events = data

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.date)
        val detail: RecyclerView = itemView.findViewById(R.id.detail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_event_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.date.text = events[position].date
        holder.detail.layoutManager = LinearLayoutManager(mContext)
        holder.detail.adapter = ContentsDetailAdapter(mContext as CalendarEventAction, events[position].detail)
    }
}