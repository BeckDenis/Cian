package com.example.cian.screens.newAd

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cian.models.Post

class NewAdViewModel : ViewModel() {

    private var _post = MutableLiveData<Post>()
    val post: LiveData<Post>
        get() = _post

    init {
        _post.value = Post()
    }
}