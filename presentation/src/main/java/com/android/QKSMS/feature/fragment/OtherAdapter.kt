package com.android.QKSMS.feature.fragment

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.android.QKSMS.R
import com.android.QKSMS.feature.compose.ComposeActivity

class OtherAdapter(private val context: Activity):RecyclerView.Adapter<OtherAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val container:ConstraintLayout = view.findViewById(R.id.container)
        val icon: ImageView = view.findViewById(R.id.icon)
        val description: AppCompatTextView = view.findViewById(R.id.description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View = LayoutInflater.from(parent.context).inflate(R.layout.attachment_other_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 5
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(position) {
            0 -> {
                holder.icon.setImageDrawable(context.resources.getDrawable(R.drawable.ic_user_oultine))
                holder.description.text = context.resources.getString(R.string.contact)
            }
            1 -> {
                holder.icon.setImageDrawable(context.resources.getDrawable(R.drawable.ic_chat_dots_icon_1))
                holder.description.text = context.resources.getString(R.string.phrase)
            }
            2 -> {
                holder.icon.setImageDrawable(context.resources.getDrawable(R.drawable.ic_time))
                holder.description.text = context.resources.getString(R.string.schedule)
            }
            3 -> {
                holder.icon.setImageDrawable(context.resources.getDrawable(R.drawable.ic_music_alt_01))
                holder.description.text = context.resources.getString(R.string.audio)
            }
            4 -> {
                holder.icon.setImageDrawable(context.resources.getDrawable(R.drawable.ic_calendar_line))
                holder.description.text = context.resources.getString(R.string.calendar)
            }
        }

        holder.container.setOnClickListener{
            val activity = context as ComposeActivity
            activity.otherClick(position)
        }
    }
}