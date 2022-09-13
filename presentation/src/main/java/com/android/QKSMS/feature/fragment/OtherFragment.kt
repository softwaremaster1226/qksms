package com.android.QKSMS.feature.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.android.QKSMS.R

class OtherFragment:Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_other, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val gridLayoutManager = GridLayoutManager(activity, 4)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.addItemDecoration(SpacesItemDecoration(15))
        recyclerView.adapter = activity?.let { OtherAdapter(it) }
        return view
    }

    class SpacesItemDecoration(private val space: Int) : ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.left = space
            outRect.right = space
            outRect.bottom = space

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.top = space
            } else {
                outRect.top = 0
            }
        }

    }
}