package com.android.QKSMS.feature.compose.editing

import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import com.android.QKSMS.common.base.QkAdapter
import com.android.QKSMS.common.base.QkViewHolder
import com.android.QKSMS.common.util.Utils
import com.android.QKSMS.databinding.ContactPlusNumberListItemBinding
import com.android.QKSMS.model.PhoneNumber

class PhoneNumberPlusAdapter : QkAdapter<PhoneNumber, ContactPlusNumberListItemBinding>() {
    var lookup: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder<ContactPlusNumberListItemBinding> {
        return QkViewHolder(parent, ContactPlusNumberListItemBinding::inflate)
    }

    override fun onBindViewHolder(holder: QkViewHolder<ContactPlusNumberListItemBinding>, position: Int) {
        val number = getItem(position)

        holder.binding.address.text = number.address
        holder.binding.type.text = number.type

        if(position == 0) {
            val params = holder.binding.root.layoutParams as RecyclerView.LayoutParams
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT
            params.bottomMargin = 28 - holder.binding.type.height / 2
            holder.binding.root.layoutParams = params
            holder.binding.check.isVisible = false
            holder.binding.divider.isVisible = false
        }

        holder.binding.root.setOnClickListener {
            holder.binding.check.isChecked = !holder.binding.check.isChecked
            if (holder.binding.check.isChecked) {
                Utils.selectedContacts[number.address] = lookup
            } else {
                Utils.selectedContacts.remove(number.address)
            }
        }
    }

    override fun areItemsTheSame(old: PhoneNumber, new: PhoneNumber): Boolean {
        return old.type == new.type && old.address == new.address
    }

    override fun areContentsTheSame(old: PhoneNumber, new: PhoneNumber): Boolean {
        return old.type == new.type && old.address == new.address
    }

}
