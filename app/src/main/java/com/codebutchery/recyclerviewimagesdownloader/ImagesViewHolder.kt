package com.codebutchery.recyclerviewimagesdownloader

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.image_entry_view.view.*

class ImagesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val ivImage: ImageView = view.ivImage
    val tvName: TextView = view.tvName
    val tvAuthor: TextView = view.tvAuthor
}