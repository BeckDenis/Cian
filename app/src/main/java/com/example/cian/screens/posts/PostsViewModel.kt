package com.example.cian.screens.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cian.models.Post

class PostsViewModel: ViewModel() {

    private var _posts = MutableLiveData(mutableSetOf<Post>())
    val posts: LiveData<MutableSet<Post>>
        get() = _posts

    fun updatePosts(post: Post) {
        _posts.value?.add(post)
        _posts.notifyObserver()
    }

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}