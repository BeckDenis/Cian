package com.example.cian.screens.newAd

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.cian.R
import com.example.cian.utils.GlideApp
import kotlinx.android.synthetic.main.item_new_ad.view.*

class NewAdAdapter(private val images: List<Uri>) :
    RecyclerView.Adapter<NewAdAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val image = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_new_ad, parent, false)
        return ViewHolder(image)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.new_ad_image.loadImage(images[position])
    }

    override fun getItemCount(): Int = images.size
}

fun ImageView.loadImage(image: Uri) =
    ifNotDestroyed {
        GlideApp.with(this).load(image).centerCrop().into(this)
    }

private fun View.ifNotDestroyed(block: () -> Unit) {
    if (!(context as Activity).isDestroyed) {
        block()
    }
}