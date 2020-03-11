package com.example.cian.screens.addeditad

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cian.models.Ad
import com.example.cian.models.PostState

class AddEditViewModel : ViewModel() {

    private var _adState = MutableLiveData(PostState.NOTHING)
    val adState: LiveData<PostState>
        get() = _adState

    fun updateAdState(state: PostState) {
        _adState.value = state
    }


    //When a new ad is created
    private var _newAd = MutableLiveData<Ad>()
    val newAd: LiveData<Ad>
        get() = _newAd

    private var _imagesNewAd = MutableLiveData(mutableMapOf<Uri, Boolean>())
    val imagesNewPost: LiveData<MutableMap<Uri, Boolean>>
        get() = _imagesNewAd

    fun updateNewAd(post: Ad) {
        _newAd.value = post
    }

    fun updateImagesNewAd(key: Uri, value: Boolean) {
        _imagesNewAd.value?.set(key, value)
        _imagesNewAd.notifyObserver()
    }

    fun deleteNewAdImage(key: Uri) {
        _imagesNewAd.value?.remove(key)
        _imagesNewAd.notifyObserver()
    }

    fun clearNewAd() {
        _newAd.value = null
        _imagesNewAd.value = mutableMapOf()
        _adState.value = PostState.NOTHING
    }


    //When an existing ad is edited
    private var _editAd = MutableLiveData<Ad>()
    val editPost: LiveData<Ad>
        get() = _editAd

    private var _imagesEditAd = MutableLiveData(mutableMapOf<Uri, Boolean>())
    val imagesEditAd: LiveData<MutableMap<Uri, Boolean>>
        get() = _imagesEditAd

    fun updateEditAd(ad: Ad) {
        _editAd.value = ad
    }

    fun deleteEditAdImage(key: Uri) {
        _imagesEditAd.value?.remove(key)
        _imagesEditAd.notifyObserver()
    }

    fun clearEditAd() {
        _editAd.value = null
        _imagesEditAd.value = mutableMapOf()
        _adState.value = PostState.NOTHING
    }


    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

}