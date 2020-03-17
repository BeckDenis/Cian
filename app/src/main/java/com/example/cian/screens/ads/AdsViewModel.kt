package com.example.cian.screens.ads

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cian.models.Ad

class AdsViewModel: ViewModel() {

    private var _ads = MutableLiveData(mutableSetOf<Ad>())
    val ads: LiveData<MutableSet<Ad>>
        get() = _ads

    fun updatePosts(post: Ad) {
        _ads.value?.add(post)
        _ads.notifyObserver()
    }

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}