package com.android.QKSMS.feature.quick

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.android.QKSMS.R
import com.android.QKSMS.common.util.Utils
import com.android.QKSMS.model.QuickMessage
import io.realm.Realm

class QuickMessageAdapter(data: ArrayList<String>):RecyclerView.Adapter<QuickMessageAdapter.ViewHolder>() {
    private val messages:ArrayList<String> = data
    private lateinit var mContext: Context

    inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        val container: ConstraintLayout = itemView.findViewById(R.id.container)
        val message: TextView = itemView.findViewById(R.id.message)
        val arrow: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.quick_message_item, parent, false)
        mContext = parent.context
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.message.text = messages[position]
        holder.container.setOnClickListener {
            (mContext as QuickMessageClick).onClick(messages[position])
        }
        holder.container.setOnLongClickListener {
            val popup = PopupMenu(mContext, holder.arrow)
            popup.inflate(R.menu.quick)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.delete -> {
                        val deleteDialog = AlertDialog.Builder(mContext)
                                .setTitle("Delete Quick Message?")
                                .setMessage("Are you sure to delete this quick message?")
                                .setPositiveButton(R.string.main_menu_delete) { dialogInterface: DialogInterface, i: Int ->
                                    Realm.getDefaultInstance().use {
                                        it.beginTransaction()
                                        val qm = it.where(QuickMessage::class.java).equalTo("messageQ", messages[position]).findFirst() as QuickMessage
                                        qm.deleteFromRealm()
                                        it.commitTransaction()
                                        messages.remove(messages[position])
                                        notifyDataSetChanged()
                                    }
                                }
                                .setNegativeButton(R.string.button_cancel, DialogInterface.OnClickListener{ dialogInterface: DialogInterface, i: Int ->
                                    return@OnClickListener
                                })
                                .create()
                        Utils.adjustAlertDialog(deleteDialog, mContext.resources.getDrawable(R.drawable.chip_background))
                        deleteDialog.show()
                        true
                    }
                    R.id.edit -> {
                        val v = (mContext as QuickMessageActivity).layoutInflater.inflate(R.layout.quick_message_add_dialog, null)
                        val et = (v.findViewById(R.id.message) as EditText)
                        et.setText(messages[position])
                        val editDialog = AlertDialog.Builder(mContext)
                                .setTitle("Edit Quick Message.")
                                .setView(v)
                                .setPositiveButton(R.string.edit) { dialogInterface: DialogInterface, i: Int ->
                                    et.requestFocus()
                                    et.setSelection(messages[position].length)
                                    if (!et.text.isNullOrEmpty()) {
                                        Realm.getDefaultInstance().use {
                                            val currentId = (it.where(QuickMessage::class.java).equalTo("messageQ", messages[position]).findFirst() as QuickMessage).id
                                            it.beginTransaction()
                                            val qmForInsert = et.text.toString()
                                            it.insertOrUpdate(QuickMessage(currentId, qmForInsert))
                                            it.commitTransaction()
                                            messages.removeAt(position)
                                            messages.add(position, qmForInsert)
                                            notifyDataSetChanged()
                                        }
                                    }
                                }
                                .setNegativeButton(R.string.button_cancel, DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
                                    return@OnClickListener
                                })
                                .create()
                        Utils.adjustAlertDialog(editDialog, mContext.resources.getDrawable(R.drawable.chip_background))
                        editDialog.show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
            return@setOnLongClickListener true
        }
    }

    fun getData():ArrayList<String> {
        return messages
    }
}