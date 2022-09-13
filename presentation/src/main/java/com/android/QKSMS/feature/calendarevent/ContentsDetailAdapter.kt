package com.android.QKSMS.feature.calendarevent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.android.QKSMS.R
import com.android.QKSMS.common.util.Utils

class ContentsDetailAdapter(private val mContext: CalendarEventAction, data: ArrayList<Utils.CalendarEventDetail>): RecyclerView.Adapter<ContentsDetailAdapter.ViewHolder>() {
    private val details = data

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val container: ConstraintLayout = itemView.findViewById(R.id.container)
        val start: TextView = itemView.findViewById(R.id.start)
        val end: TextView = itemView.findViewById(R.id.end)
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
        val check: CheckBox = itemView.findViewById(R.id.check)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_event_detail_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return details.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.start.text = details[position].startDate.split(" ")[1]
        holder.end.text = details[position].endDate.split(" ")[1]
        holder.title.text = details[position].nameOfEvents
        holder.description.text = details[position].description
        holder.container.setOnClickListener {
            holder.check.isChecked = !holder.check.isChecked
            mContext.insertToSelected(details[position])
        }
    }
}