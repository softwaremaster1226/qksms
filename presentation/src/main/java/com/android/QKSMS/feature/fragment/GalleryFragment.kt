package com.android.QKSMS.feature.fragment

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.QKSMS.R
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding2.view.selected
import java.util.*

class GalleryFragment : Fragment(), GalleryAdapter.GalleryClickListeners {
    private lateinit var adapter: GalleryAdapter
    private lateinit var attaching: CameraFragment.PhotoFromCamera
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)
        val done = view.findViewById<ImageView>(R.id.done)

        val photoRecyclerView = view.findViewById<RecyclerView>(R.id.gallery)

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"
        val cursor = activity?.contentResolver?.query(uri, projection, null, null, sortOrder)
        val uriList: ArrayList<Uri> = ArrayList()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                val baseUri = Uri.parse("content://media/external/images/media")
                uriList.add(Uri.withAppendedPath(baseUri, "" + id))
            }
            cursor.moveToPosition(-1) //Restore cursor back to the beginning
        }
        adapter = GalleryAdapter(view, uriList, this)
        photoRecyclerView.layoutManager = GridLayoutManager(view.context, 3)
        photoRecyclerView.adapter = adapter

        done.setOnClickListener{
            adapter.getSelections().forEach {
                attaching.setPhotoFromCamera(it)
            }
        }
        return view
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        attaching = activity as CameraFragment.PhotoFromCamera
    }

    override fun onClick(data: ArrayList<Uri>) {
        data.forEach{
            attaching.setPhotoFromCamera(it)
        }
    }
}