package com.example.cian.screens.addeditad

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cian.R
import com.example.cian.utils.loadImage
import kotlinx.android.synthetic.main.item_new_ad.view.*

class AddEditAdAdapter(private val images: List<Uri>, val onClickListener: (Uri) -> Unit) :
    RecyclerView.Adapter<AddEditAdAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_new_ad, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.run{
            item_new_ad_image.loadImage(images[position])
            item_new_ad_close_image.setOnClickListener { onClickListener(images[position]) }
        }
    }

    override fun getItemCount(): Int = images.size

}