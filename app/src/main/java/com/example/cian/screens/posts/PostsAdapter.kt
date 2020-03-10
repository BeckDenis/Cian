package com.example.cian.screens.posts

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cian.R
import com.example.cian.models.Post
import com.example.cian.utils.OnPageChangeListenerAdapter
import com.example.cian.utils.ViewPagerAdapter
import kotlinx.android.synthetic.main.item_post.view.*

class PostsAdapter(private val posts: List<Post>, val onClickListener: (String) -> Unit) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    companion object {
        private val TAG = PostsAdapter::class.java.simpleName
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(TAG, posts.toString())
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        val images = post.images?.map { it.value }

        holder.view.run {
            item_post_description.text = post.shortDescription
            item_post_price.text = post.price.toString()
            val viewPagerAdapter = ViewPagerAdapter(context, images) { onClickListener(post.id) }
            item_post_viewpager.adapter = viewPagerAdapter

            if (images.isNullOrEmpty()) item_post_images_count.visibility = View.GONE else {
                item_post_images_count.run {
                    visibility = View.VISIBLE
                    text = resources.getString(R.string.images_count, 1, images.size)
                }
                item_post_viewpager.addOnPageChangeListener(OnPageChangeListenerAdapter { position ->
                    holder.view.item_post_images_count.text =
                        resources.getString(R.string.images_count, (position + 1), images.size)
                })
            }
            setOnClickListener { onClickListener(post.id) }
        }
    }

    override fun getItemCount(): Int = posts.size
}