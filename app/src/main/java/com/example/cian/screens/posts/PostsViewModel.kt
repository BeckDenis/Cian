package com.example.cian.screens.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cian.models.Ad

class PostsViewModel: ViewModel() {

    private var _posts = MutableLiveData(mutableSetOf<Ad>())
    val posts: LiveData<MutableSet<Ad>>
        get() = _posts

    fun updatePosts(post: Ad) {
        _posts.value?.add(post)
        _posts.notifyObserver()
    }

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}