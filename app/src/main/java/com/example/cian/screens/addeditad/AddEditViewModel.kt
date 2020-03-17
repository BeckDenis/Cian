package com.example.cian.screens.addeditad

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cian.models.Ad

class AddEditViewModel : ViewModel() {

    private var _adState = MutableLiveData(AdState.NOTHING)
    val adState: LiveData<AdState>
        get() = _adState

    fun updateAdState(state: AdState) {
        _adState.value = state
    }


    private var _ad = MutableLiveData<Ad>()
    val ad: LiveData<Ad>
        get() = _ad

    private var _images = MutableLiveData(mutableMapOf<Any, ImageState>())
    val images: LiveData<MutableMap<Any, ImageState>>
        get() = _images

    fun updateAd(ad: Ad?) {
        ad?.let { _ad.value = it }
    }

    fun updateImages(key: Any?, value: ImageState) {
        key?.let{
            _images.value?.set(it, value)
            _images.notifyObserver()
        }
    }

    fun deleteImage(key: Any) {
        _images.value?.remove(key)
        _images.notifyObserver()
    }

    fun clear() {
        _ad.value = null
        _images.value = mutableMapOf()
        _adState.value = AdState.NOTHING
    }


    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

}