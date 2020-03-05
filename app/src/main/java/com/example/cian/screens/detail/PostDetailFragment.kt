package com.example.cian.screens.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs

import com.example.cian.R
import com.example.cian.models.Post
import com.example.cian.utils.FirebaseHelper
import com.example.cian.utils.ValueEventListenerAdapter
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

        firebase.databasePost(args.postId)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                val post = it.getValue(Post::class.java)
                if (post != null) {
                    post_detail_short_description_input.text = post.shortDescription
                    post_detail_full_description_input.text = post.fullDescription
                    post_detail_price_input.text = post.price.toString()
                }
            })
    }

}
