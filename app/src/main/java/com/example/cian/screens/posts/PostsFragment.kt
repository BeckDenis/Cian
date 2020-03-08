package com.example.cian.screens.posts

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import com.example.cian.R
import com.example.cian.models.Post
import com.example.cian.utils.FirebaseHelper
import com.example.cian.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.fragment_posts.*

class PostsFragment : Fragment(R.layout.fragment_posts) {

    companion object{
        val TAG = PostsFragment::class.java.simpleName
    }

    private lateinit var firebase: FirebaseHelper
    private val args by navArgs<PostsFragmentArgs>()
    private val viewModel by activityViewModels<PostsViewModel>()
    private var postsId: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebase = FirebaseHelper()

        if (savedInstanceState == null)

        if (args.postsId != null && firebase.currentUser != null) {
            postsId = args.postsId?.toList()
            getPosts()
        } else currentUserPostsId()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.posts.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "${it.size} ${postsId?.size}")
            if (it.size == postsId?.size) updateRecycler(it)
        })
    }

    private fun updateRecycler(posts: MutableSet<Post>) {
        Log.d(TAG, "posts: $posts")
        if (!posts.isNullOrEmpty()) {
            posts_recycler.adapter = PostsAdapter(posts.toList()) {
                findNavController().navigate(PostsFragmentDirections.actionPostsToPostDetail(it))
            }
            Log.d(TAG, "updateRecycler called")
        }
    }

    private fun getPosts() {
        Log.d(TAG, "getPosts called")
        postsId!!.forEach { postId ->
            firebase.databasePost().child(postId)
                .addListenerForSingleValueEvent(ValueEventListenerAdapter { postData ->
                    val post = postData.getValue(Post::class.java)?.copy(id = postData.key!!)
                    if (post != null) {
                        viewModel.updatePosts(post)
                    }
                })
        }
    }

    private fun currentUserPostsId() {
        firebase.databaseUserPost(firebase.currentUser!!.uid)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter { postsData ->
                postsId = postsData.children.map { it.value as String }
                getPosts()
            })
    }
}
