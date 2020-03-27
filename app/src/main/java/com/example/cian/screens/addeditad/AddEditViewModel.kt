package com.example.cian.screens.addeditad

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cian.extesions.notifyObserver
import com.example.cian.models.Ad
import com.google.android.gms.maps.model.LatLng

class AddEditViewModel : ViewModel() {

    // Ad State
    private var _adState = MutableLiveData(AdState.NOTHING)
    val adState: LiveData<AdState>
        get() = _adState

    fun updateAdState(state: AdState) {
        _adState.value = state
    }

    // Ad
    private var _ad = MutableLiveData(Ad())
    val ad: LiveData<Ad>
        get() = _ad

    fun updateAd(ad: Ad?) {
        ad?.let { _ad.value = it }
    }

    //Images
    private var _images = MutableLiveData(mutableMapOf<Any, ImageState>())
    val images: LiveData<MutableMap<Any, ImageState>>
        get() = _images

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

    // Other
    fun clear() {
        _ad.value = Ad()
        _images.value = mutableMapOf()
        _adState.value = AdState.NOTHING
    }

}