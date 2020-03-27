package com.example.cian.screens.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cian.extesions.notifyObserver
import com.example.cian.models.Ad

class AccountViewModel: ViewModel() {
    private var _ads = MutableLiveData(mutableSetOf<Ad>())
    val ads: LiveData<MutableSet<Ad>>
        get() = _ads

    fun updateAds(ad: Ad) {
        _ads.value?.add(ad)
        _ads.notifyObserver()
    }
}