package com.example.cian.screens.detail

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cian.R
import com.example.cian.models.Post
import com.example.cian.utils.FirebaseHelper
import com.example.cian.utils.ValueEventListenerAdapter
import com.example.cian.utils.ViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_post_detail.*

class PostDetailFragment : Fragment(R.layout.fragment_post_detail) {

    companion object{
        private val TAG = PostDetailFragment::class.java.simpleName
    }

    private val args by navArgs<PostDetailFragmentArgs>()
    private lateinit var firebase: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebase = FirebaseHelper()

        if (firebase.checkAuth()) {
            firebase.databaseUserPost(firebase.currentUser!!.uid)
                .addListenerForSingleValueEvent(ValueEventListenerAdapter { postsId ->
                    if (postsId.children.map { it.getValue(String::class.java) }
                            .contains(args.postId)) setHasOptionsMenu(true)
                })
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.edit, menu)

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menu_edit -> {
                val action = PostDetailFragmentDirections.actionPostDetailToAddEditAd(
                    args.postId, getString(R.string.edit_ad_title)
                )
                findNavController().navigate(action)
                true
            }
            else -> false
        }

}
