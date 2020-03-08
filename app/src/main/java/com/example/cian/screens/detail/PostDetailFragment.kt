package com.example.cian.screens.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.navArgs
import com.example.cian.R
import com.example.cian.models.Post
import com.example.cian.utils.FirebaseHelper
import com.example.cian.utils.ValueEventListenerAdapter
import com.example.cian.utils.ViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_post_detail.*

class PostDetailFragment : Fragment(R.layout.fragment_post_detail) {

    private val args by navArgs<PostDetailFragmentArgs>()
    private lateinit var firebase: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebase = FirebaseHelper()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebase.databasePost().child(args.postId)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter { data ->
                val post = data.getValue(Post::class.java)
                if (post != null) {
                    post_detail_short_description_text.text = post.shortDescription
                    post_detail_full_description_text.text = post.fullDescription
                    post_detail_price_text.text = post.price.toString()
                    val adapter =
                        ViewPagerAdapter(requireContext(), post.images?.map { it.value }) {}
                    post_detail_view_pager.adapter = adapter

                }
            })
    }

}
