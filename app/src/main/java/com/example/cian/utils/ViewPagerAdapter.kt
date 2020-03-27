package com.example.cian.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.example.cian.R

class ViewPagerAdapter(
    val context: Context, private val images: List<String>?, val onClickListener: () -> Unit
) : PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun getCount(): Int = if (images.isNullOrEmpty()) 1 else images.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        val image: Any? = if (images.isNullOrEmpty()) {
            context.getDrawable(R.drawable.place_holder)
        } else images[position]
        GlideApp.with(context).load(image).fallback(R.drawable.place_holder).into(imageView)
        container.addView(imageView, 0)
        imageView.setOnClickListener { onClickListener() }
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ImageView)
    }

}