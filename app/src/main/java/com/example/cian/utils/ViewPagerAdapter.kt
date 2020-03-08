package com.example.cian.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter
import com.example.cian.R
import com.example.cian.utils.GlideApp

class ViewPagerAdapter(val context: Context, private val images: List<String>, val onClickListener: () -> Unit): PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int = images.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        GlideApp.with(context).load(images[position]).fallback(R.drawable.place_holder).into(imageView)
        container.addView(imageView, 0)
        imageView.setOnClickListener { onClickListener() }
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ImageView)
    }
}