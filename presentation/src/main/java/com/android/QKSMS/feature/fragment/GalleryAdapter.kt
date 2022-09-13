package com.android.QKSMS.feature.fragment

import android.app.Activity
import android.net.Uri
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.android.QKSMS.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jakewharton.rxbinding2.view.longClicks
import java.util.*
import kotlin.collections.ArrayList

class GalleryAdapter(private val context: View, uris: ArrayList<Uri>, listeners: GalleryClickListeners) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    private val mUris: ArrayList<Uri> = uris
    private val mListeners = listeners
    private val mSelections = ArrayList<Uri>()
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_photo, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        Glide.with(context)
                .load(mUris[i])
                .apply(RequestOptions.centerCropTransform())
                .into(viewHolder.img)

        viewHolder.img.setOnClickListener {
            viewHolder.check.isChecked = !viewHolder.check.isChecked
            if(viewHolder.check.isChecked) {
                mSelections.add(mUris[i])
            } else {
                mSelections.remove(mUris[i])
            }
        }
    }

    override fun getItemCount(): Int {
        return mUris.size
    }

    fun getSelections(): ArrayList<Uri> {
        return mSelections
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var img: ImageView = view.findViewById(R.id.photos)
        var check: CheckBox = view.findViewById(R.id.check)

        init {
            val metrics = DisplayMetrics()
            (view.context as Activity).windowManager.defaultDisplay.getMetrics(metrics)
            img.layoutParams.width = metrics.widthPixels / 3
            img.layoutParams.height = metrics.widthPixels / 3
        }
    }

    interface GalleryClickListeners {
        fun onClick(data: ArrayList<Uri>)
    }

}